package org.card.scrabble

class Rack {
    def board
    def tiles = []
    
    Rack(board){
        this.board = board
    }
    
    def applyRack(tileString){
        this.tiles = tileString.split() - "TILES:"
    }
    
    def buildPlays(){
        def bestPlay
        def bestScore
        
        // Iterate over all row plays
        (0..14).each{ row ->
            board.getRowAnchors(row).each{ anchor ->
                def lspace = anchor.getLeftSpace()
                (0..lspace).each { offset ->
                    def playableWords = []
                    def start = board.rows[row][anchor.colPos-offset]
                    def hardLimit = 15 - start.colPos
                    def rules = start.getRowRules()
                    Lexicon.getInstance().getAllMatches(rules, start.getRowIllegalLengths(), hardLimit, tiles).each { word ->
                        def p = new Play(board, board.rows, start.rowPos, start.colPos, word)
                        if(p.scorePlay() > bestScore){
                            bestScore = p.scorePlay()
                            bestPlay = p
                        }
                    }
                }
            }
        }
        
        // Iterate over all column plays
        (0..14).each{ column ->
            board.getColumnAnchors(column).each{ anchor ->
                def lspace = anchor.getTopSpace()
                (0..lspace).each { offset ->
                    def playableWords = []
                    def start = board.cols[column][anchor.rowPos-offset]
                    def hardLimit = 15 - start.rowPos
                    def rules = start.getColumnRules()
                    Lexicon.getInstance().getAllMatches(rules, start.getColumnIllegalLengths(), hardLimit, tiles).each { word ->
                        def p = new Play(board, board.cols, start.colPos, start.rowPos, word)
                        if(p.scorePlay() > bestScore){
                            bestScore = p.scorePlay()
                            bestPlay = p
                        }
                    }
                }
            }
        }
        
        return bestPlay
    }
    
}

