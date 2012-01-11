package org.card.scrabble

class Lexicon {
    static def lexicon = new Lexicon()
    
    def words = []
    def wordsByLetter = [:]
    
    static Lexicon getInstance(){
        return lexicon
    }
    
    Lexicon(){
        words = Lexicon.class.classLoader.getResourceAsStream("dictionary.txt").readLines()
        wordsByLetter = ('A'..'Z').collectEntries{
            [it, (0..15).collectEntries{ [it,[]]}]
        }
        words.each{ word ->
            word.toList().eachWithIndex { it, i -> 
                wordsByLetter[it][i] << word
            }
            
        }
    }
    
    /**
     * Get all words that can be formed from a given rack. This is primarily useful
     * only for the first turn, since typically there are other considerations to
     * determine what is playable.
     */
    def getAllWords(letters){
        
        def wildcards = letters.count("*")
        letters -= "*"
        
        words.findAll{
            // early-out condition
            if(it.length() > letters.size() + wildcards) return false
            
            def copy = letters.clone();
            def countNotFound = 0
            def canPlay = true;
            it.toList().collect{
                if(!copy.remove(it)) countNotFound++;
                if(countNotFound > wildcards){
                    // word is untenable
                    canPlay = false;
                }
            }
            canPlay
        }
    }
  
    
    /**
     * Get a starting word set for match evaluation.
     */
    def getStartingSet(position, letters){
        def startingWordSet = []
        ([]+letters).each{
            startingWordSet.addAll(wordsByLetter[it][position])
        }
        return startingWordSet
    }
    
    def getAllMatches(rules, illegalLengths, hardLimit, tiles){
        def matches = []
        def head = rules.find{ true }
        def startingSet = getStartingSet(head.key, head.value)
        startingSet.each{ word ->
            def play = wordIsValid(rules, illegalLengths, hardLimit, tiles, word)
            if(play != null){
                matches << play
            }
        }
        return matches
    }
    
    def wordIsValid(rules, illegalLengths, hardLimit, tiles, word){
        def isMatch = true
        def play = ""
        int wildcards = tiles.count("*")
        def copy = ([] + tiles).flatten();
        copy -= "*"
        
        // TODO : re-write to break early
        if(word.length() > hardLimit || illegalLengths.contains(word.length())) isMatch = false
      
            
        word.toList().eachWithIndex{ letter, i -> 
            def valid = rules[i]
            def newTile = true    
            
            // handle cases where there are constraints on thre current letter
            if(valid != null && valid instanceof String){
                newTile = false
                if(valid != letter){
                    isMatch = false
                } else {
                    play += "($letter)"
                }
            } else if(valid != null && !valid.contains(letter)) {
                isMatch = false
            } 
            
            if(newTile && isMatch){
                // play a tile, if we can
                if(!copy.remove(letter)) {
                    if(wildcards > 0) {
                        play += letter.toLowerCase()
                        wildcards--
                    } else {
                        isMatch = false
                    }
                } else {
                    play += letter
                }
            }
        }
        return isMatch ? play : null
    }
    
    /** 
     * Useful for determining single-letter add-ons. This method finds all single
     * letters that can be prefixed to a given ending.
     */
    def validPrefixes(end){
        def result = words.findAll{ 
            it.substring(1).equals(end)
        }.collect{
            it.getAt(0)
        };
        return result
    }
    
    /** 
     * Useful for determining single-letter add-ons. This method finds all single
     * letters that can be suffixed to a given start.
     */
    def validSuffixes(start){
        def len = start.length() + 1
        def result = words.findAll{ 
            it.length() == len && it.startsWith(start)
        }.collect{
            it.getAt(it.length()-1)
        };
        return result
    }
}

