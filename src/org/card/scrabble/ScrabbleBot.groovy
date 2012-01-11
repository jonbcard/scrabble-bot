package org.card.scrabble

Board b = new Board()
Rack  r = new Rack(b)

System.in.withReader { 
    // bootstrap the dictionary right off the bat to save precious turn time later
    // This implementation is SLOW!
    Lexicon.getInstance()
    it.readLine() // don't really care what player number we are -- doesn't change strategy
    println "jc CardBot"
    while(line = it.readLine() ){
        if(line.equals("EXIT")) {
            System.exit(0)
        } else if (line.startsWith("TILES")) {
            r.applyRack(line)
        } else if (line.startsWith("PLAY")) {
            b.playMove(line)
        } else if (line.startsWith("NEXT")) {
            def bestPlay = r.buildPlays()
            if(bestPlay == null){ 
                def tileString = r.tiles.join(" ")
                println "EXCHANGE: $tileString"
            } else {
                println bestPlay.playString()
            }
        }
    }
}  

