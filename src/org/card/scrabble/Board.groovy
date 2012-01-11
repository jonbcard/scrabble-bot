package org.card.scrabble

class Board {
    /** Pattern to test which way a play is going. */
    def letterScores = [:]
    def dirPattern = ~/^[0-9]+[A-z]/
    def alphaMap = [:]
    def firstMove = true
    
    def rows = [][]
    def cols = [][]
    
    Board(){
        // Setup Alpa-map
        ('A'..'O').eachWithIndex{ it, i -> 
            alphaMap[it] = i  
        }
        
        // Setup scores
        ('a'..'z').each{ letterScores[it]=0 }
        ['A','E','I','L','O','N','R','S','T','U'].each{ letterScores[it]=1 }
        ['D','G'].each{ letterScores[it]=2 }
        ['B','C','M','P'].each{ letterScores[it]=3 }
        ['F','H','V','W','Y'].each{ letterScores[it]=4 }
        ['K'].each{ letterScores[it]=5 }
        ['J','X'].each{ letterScores[it]=8 }
        ['Q','Z'].each{ letterScores[it]=10 }
        
        // Init squares
        (0..14).each{ x ->
            rows << []
            (0..14).each{ y ->
                rows[x] << new Square(this, x, y)
            }
        }
        cols = GroovyCollections.transpose(rows)
        
        // setup triple words
        (0..2).each{ x ->
            (0..2).each{ y ->
                rows[x*7][y*7].wordMultiplier = 3
            }
        }
        
        // setup double words
        (1..4).each{
            rows[it][it].wordMultiplier = 2
            rows[14-it][it].wordMultiplier = 2
            rows[it][14-it].wordMultiplier = 2
            rows[14-it][14-it].wordMultiplier = 2
        }
        rows[7][7].wordMultiplier = 2
        
        // setup triple letters
        (0..3).each{
            rows[it*4+1][5].letterMultiplier = 3
            rows[it*4+1][9].letterMultiplier = 3
        }
        rows[5][1].letterMultiplier = 3
        rows[9][1].letterMultiplier = 3
        rows[5][13].letterMultiplier = 3
        rows[9][13].letterMultiplier = 3
        
        // setup double letters
        (0..2).each{ i ->
            [3,11].each{j ->
                rows[i*7][j].letterMultiplier = 2
                rows[i*7][j].letterMultiplier = 2
                cols[i*7][j].letterMultiplier = 2
                cols[i*7][j].letterMultiplier = 2
            }
        }
        [6,8].each{ i->
            [2,12].each { j->
                rows[i][j].letterMultiplier = 2
                cols[i][j].letterMultiplier = 2
            }
            [6,8].each { j-> rows[i][j].letterMultiplier = 2 }
        }
        
        // Finally, setup the middle tile for play start
        rows[7][7].rowAddons = ('A'..'Z')
        rows[7][7].columnAddons = ('A'..'Z')
        
    }
    
    def getDirectionAndCoords(String pos){
        return new Object(){
            def dir = (pos ==~ dirPattern ? rows : cols)
            def coord1 = (dir == rows) ? pos.substring(0, pos.length()-1).toInteger() - 1 : alphaMap[pos.getAt(0)]
            def coord2 = (dir == rows) ? alphaMap[pos.getAt(pos.length()-1)] :  pos.substring(1).toInteger() - 1
        }
    }
    
    /**
     * Play a move using the given definition provided by the harness. This will 
     * be parsed, then applied to the board.
     */
    def playMove(play){
        if(firstMove) firstMove = false
        
        def (_, player, pos, word, points) = play.split()
        def c = getDirectionAndCoords(pos)
        
        // first, add the word...
        word.toList().eachWithIndex{ it, i -> 
            c.dir[c.coord1][c.coord2+i].value = it
        }
        // ... then adjust single character add-ons for adjacents
        word.toList().eachWithIndex{ it, i -> 
            c.dir[c.coord1][c.coord2+i].updateAdjacent()
        }
    }
    
    /**
     * An 'anchor' is considered to be any existing tile in the row that we can 
     * play off of.
     */
    def getRowAnchors(rowNum){
        def anchors = []
        rows[rowNum].eachWithIndex{ it, i -> 
            def candidate = i != 14 && 
                   it.value != null || 
                   (it.rowAddons != null && it.rowAddons.size() > 0)
            // exclude candidates that have an real tile to their left. In this case, the
            // tile to the left is the anchor
            if(candidate && (i == 0 || rows[rowNum][i-1].value == null) ){
                anchors << it
            }
        }
        return anchors
    }
    
    def getColumnAnchors(colNum){
        def anchors = []
         cols[colNum].eachWithIndex{ it, i -> 
             def candidate = i != 14 && 
                    it.value != null || 
                    (it.columnAddons != null && it.columnAddons.size() > 0)
             // exclude candidates that have an real tile on top of them. In this case, the
             // tile on top is the anchor
             if(candidate && (i == 0 || cols[colNum][i-1].value == null) ){
                 anchors << it
             }
         }
         return anchors
    }
    
    def printBoard(){
        rows.each{ x ->
            x.each{ y ->
                print "$y"
            }
            println ""
        }
    }
}

