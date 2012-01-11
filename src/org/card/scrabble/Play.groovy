package org.card.scrabble

class Play {
    def board
    
    def x
    def y
    
    def vertex
    def word
    
    def Play(Board board, String play){
        def (pos, word) = play.split()
    }
    
    def Play(Board board, vertex, int x, int y, String word){
        this.board = board
        this.vertex = vertex
        this.x = x
        this.y = y
        this.word = word
    }
    
    def scorePlay(){
        // strip brackets for scoring
        def wordNoParens = word.replaceAll("\\(","").replaceAll("\\)","")
        
        
        def tilesPlayed = 0
        def wordMultipliers = []
        
        def score = 0
        def adjacentWordScores = 0
        
        // score 
        def startingPoint = vertex[x][y]
        for(int i=0; i < wordNoParens.length(); i++){
            def square = vertex[x][y+i]
            def letter = wordNoParens.getAt(i)
            if(square.value == null){
                // indicates this is a new tile and so multipliers count towards the score
                // and adjacent words will count towards our score
                if(board.letterScores[letter] == null){
                    println("SCORE NOT FOUND FOR::::: $letter")
                    println board.letterScores
                }
                score +=  board.letterScores[letter] * square.letterMultiplier
                if(square.wordMultiplier > 1) wordMultipliers << square.wordMultiplier
                adjacentWordScores += scoreAdjacent(square, letter)
                tilesPlayed++
            } else {
                // old tile -- only count the base value of the tile and do not
                // score any adjacent words
                score += board.letterScores[letter]
            }

            
        }
        
        wordMultipliers.each{
            score *= it
        }
        score += adjacentWordScores
        if(tilesPlayed == 7) score += 50 // Add bingo
        return score
    }
    
    /**
     * Get score for secondary words that were created out of the given square.  
     */ 
    int scoreAdjacent(square, letter){
        int score = 0
        if(vertex==board.rows){
            (square.getLettersAbove() + square.getLettersBelow()).toList().each{
                score += board.letterScores[it]
                
            }
        } else {
            // column play
            (square.getLettersToLeft() + square.getLettersToRight()).toList().each{
                score += board.letterScores[it]
            }
        }
        if(score > 0){
            // this means that adjacent squares were found.
            score += (board.letterScores[letter]*square.letterMultiplier)
            score *= square.wordMultiplier
        }
        return score;
    }
    
    def playString(){
        def col = (vertex == board.rows) ? y : x
        def colAlpha = board.alphaMap.find{it.value == col}?.key
        def row  = (vertex == board.rows) ? x : y
        def position = (vertex == board.rows) ? "${row+1}$colAlpha" : "$colAlpha${row+1}" 
        return "$position $word"
    }
    
    String toString(){
        def dir = (vertex == board.rows) ? "ACROSS" : "DOWN"
        def score = scorePlay()
        "Play: {$x,$y $dir:$word ($score)}"
    }
}
