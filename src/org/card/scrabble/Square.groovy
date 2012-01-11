package org.card.scrabble

class Square {
    /** Outer reference to the board */
    def board
    /** Row position of this square (0-based index) */
    def rowPos
    /** Column position of this square (0-based index) */
    def colPos
    /** Used for point calculation. Valid values are: 1, 2, 3. */
    def wordMultiplier = 1
    /** Used for point calculation. Valid values are: 1, 2, or 3. */
    def letterMultiplier = 1
    
    /**
     * Valid single tile add-ons for this square if playing against a row. Here 
     * null means that play on the square is open, while an empty array effectively
     * means that there are no single character add-ons
     */
    def rowAddons = null
    
    /**
     * Valid single tile add-ons for this square if playing against a column. Here 
     * null means that play on the square is open, while an empty array effectively
     * means that there are no single character add-ons
     */
    def columnAddons = null
    
    /** 
     * Current tile in this position. This is a mutable value -- when a tile is
     * played on the square, the value will be applied.  
     */
    def value
    
    Square(board, rowPos, colPos){
        this.board = board
        this.rowPos = rowPos
        this.colPos = colPos
    }
    
    def getBlankAdjacent(){
        def adj = [];
        def blank = getNextBlankAbove()
        if(blank != null) adj << blank
        blank = getNextBlankBelow()
        if(blank != null) adj << blank
        blank = getNextBlankToLeft()
        if(blank != null) adj << blank
        blank = getNextBlankToRight()
        if(blank != null) adj << blank
        
        return adj
    }
    
    def updateAdjacent() {
        getBlankAdjacent().each{
            it.applyValidMoves()
        }       
    }
    
    def getRowIllegalLengths(){
        def lengths = []
        (colPos..14).eachWithIndex{ pos, i -> 
            if(i > 0  && board.rows[rowPos][pos].value != null){
                lengths << i
            }
        }
        return lengths
    }
    
    def getColumnIllegalLengths(){
        def lengths = []
        (rowPos..14).eachWithIndex{ pos, i -> 
            if(i > 0  && board.cols[colPos][pos].value != null){
                lengths << i
            }
        }
        return lengths
    }
    
    
    def getRowRules(){
        def rules = [:]
        (colPos..14).eachWithIndex{ pos, i -> 
            if(board.rows[rowPos][pos].value != null){
                rules[i] = board.rows[rowPos][pos].value
                
            } else if (board.rows[rowPos][pos].rowAddons != null){
                rules[i] = board.rows[rowPos][pos].rowAddons
            }
        }
        return rules
    }
    
    def getColumnRules(){
        def rules = [:]
        (rowPos..14).eachWithIndex{ row, i -> 
            if(board.cols[colPos][row].value != null){
                rules[i] = board.cols[colPos][row].value
            } else if (board.cols[colPos][row].columnAddons != null){
                rules[i] = board.cols[colPos][row].columnAddons
            }
        }
        return rules
    }
    
    def getNextBlankAbove(){
        for(int i=rowPos-1; i>=0; i--){
            if(board.rows[i][colPos].value == null){
                return board.rows[i][colPos]
            }
        }
        return null
    }
    
    def getLettersAbove(){
        def topWord = ""
        def pos = rowPos-1
        while(pos >= 0 && board.rows[pos][colPos].value != null){
            topWord = board.rows[pos][colPos].value + topWord
            pos--
        }
        return topWord
    }
    
    def getNextBlankBelow(){
        for(int i=rowPos+1; i<=14; i++){
            if(board.rows[i][colPos].value == null){
                return board.rows[i][colPos]
            }
        }
        return null
    }
    
    def getLettersBelow(){
        def bottomWord = ""
        def pos = rowPos+1
        while(pos <= 14 && board.rows[pos][colPos].value != null){
            bottomWord += board.rows[pos][colPos].value
            pos++
        }
        return bottomWord
    }
    
    def getNextBlankToLeft(){
        for(int i=colPos-1; i>=0; i--){
            if(board.cols[i][rowPos].value == null){
                return board.cols[i][rowPos]
            }
        }
        return null
    }
    
    def getLettersToLeft(){
        def leftWord = ""
        def pos = colPos-1
        while(pos >= 0 && board.rows[rowPos][pos].value != null){
            leftWord = board.rows[rowPos][pos].value + leftWord
            pos--
        }
        return leftWord
    }
    
    def getNextBlankToRight(){
        for(int i=colPos+1; i<=14; i++){
            if(board.cols[i][rowPos].value == null){
                return board.cols[i][rowPos]
            }
        }
        return null
    }
    
    def getLettersToRight(){
        def rightWord = ""
        def pos = colPos+1
        while(pos <= 14 && board.rows[rowPos][pos].value != null){
            rightWord += board.rows[rowPos][pos].value
            pos++
        }
        return rightWord
    }
    
    /**
     * Based on existing adjacent tiles to a square, apply column and row addons
     * that can be used on this square. This should be called once after each to
     * so that the values are calculated in advance when we are trying to figure 
     * out our play.
     */
    def applyValidMoves(){
        //println("Checking adjacent for: ($rowPos, $colPos)" )
        
        //handle row add-ons
        def topWord = getLettersAbove()
        def bottomWord = getLettersBelow()
        
        if(topWord.length() > 0 && bottomWord.length() > 0){
            // TODO: rare but acceptable case that should be handled. For now
            // we'll assume that nothing can be played here.
            rowAddons = []
        } else if(topWord.length() > 0){
            rowAddons = Lexicon.getInstance().validSuffixes(topWord)
        } else if (bottomWord.length() > 0){
            rowAddons = Lexicon.getInstance().validPrefixes(bottomWord)
        } else {
            // nothing to do
        }
        
        // handle column addons
        def leftWord = getLettersToLeft()
        def rightWord = getLettersToRight()
        
        if(leftWord.length() > 0 && rightWord.length() > 0){
            // TODO: rare but acceptable case that should be handled. For now
            // we'll assume that nothing can be played here.
            columnAddons = []
        } else if(leftWord.length() > 0){
            columnAddons = Lexicon.getInstance().validSuffixes(leftWord)
        } else if (rightWord.length() > 0){
            columnAddons = Lexicon.getInstance().validPrefixes(rightWord)
        } else {
            // nothing to do
        }
        
    }
    
    def getLeftSpace(){
        def tilesRequired = (value == null) ? 1 : 0
        
        def pos = colPos
        def i=0
        while(--pos >= 0 && board.rows[rowPos][pos].value == null && board.rows[rowPos][pos].rowAddons == null){
            // make sure this isn't joining on to another word before counting this as a space
            if(pos==0 || board.rows[rowPos][pos-1].value == null) i++
            if(++tilesRequired == 7) return i // early out when tile count is exceeded
        }
        return i
    }
    
    def getRightSpace(rackSize){
        tilesLeft = rackSize
        
        def pos = colPos
        def i=0
        while(pos++ <= 14 && tilesLeft > 0){
            if(board.rows[rowPos][pos].rowAddons==[]){
                break
            }
            if(board.rows[rowPos][pos].value == null) tilesLeft--;
            i++
        }
        return i
    }
    
    def getTopSpace(){
        def tilesRequired = (value == null) ? 1 : 0
        def pos = rowPos
        def i=0
        while(--pos >= 0 && board.cols[colPos][pos].value == null && board.cols[colPos][pos].columnAddons == null){
            // make sure this isn't joining on to another word before counting this as a space
            if(pos==0 || board.cols[colPos][pos-1].value == null) i++
            if(++tilesRequired == 7) return i // early out when tile count is exceeded
        }
        return i
    }
    
    def String toString() {
        if(value) return " $value "
        if(wordMultiplier>1) return " W$wordMultiplier"
        if(letterMultiplier>1) return " L$letterMultiplier"
        return " --"
    }
    
}