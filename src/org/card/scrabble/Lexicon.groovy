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
        // early-out for the starting turn case
        if (letters == ('A'..'Z')) return words
        
        def startingWordSet = []
        ([]+letters).each{
            startingWordSet.addAll(wordsByLetter[it.toUpperCase()][position])
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
        def newWordCreated = false
        def play = ""
        int wildcards = tiles.count("*")
        def copy = ([] + tiles).flatten();
        copy -= "*"
        
        // TODO : re-write to break early
        if(word.length() > hardLimit || illegalLengths.contains(word.length())) isMatch = false
        
        def wordArr = word.toList()
        for(int i=0;i<wordArr.size();i++){
            def letter = wordArr[i]
        
            def valid = rules[i]
            def newTile = true    
            
            // handle cases where there are constraints on thre current letter
            if(valid != null && valid instanceof String){
                newTile = false
                if(valid.toUpperCase() != letter){
                    isMatch = false
                    break
                } else {
                    play += "($valid)"
                }
            } else if(valid != null && !valid.contains(letter)) {
                isMatch = false
                break
            } 
            
            if(newTile && isMatch){
                // play a tile, if we can
                if(!copy.remove(letter)) {
                    if(wildcards > 0) {
                        newWordCreated = true
                        play += letter.toLowerCase()
                        wildcards--
                    } else {
                        isMatch = false
                        break
                    }
                } else {
                    newWordCreated = true
                    play += letter
                }
            }
        }
        return isMatch && newWordCreated ? play : null
    }
    
    /** 
     * Useful for determining single-letter add-ons. This method finds all single
     * letters that can be prefixed to a given ending.
     */
    def validPrefixes(end){
        end = end.toUpperCase()
        def lastLetter = end.getAt(end.length()-1)
        def len = end.length() + 1
        def result = wordsByLetter[lastLetter.toUpperCase()][end.length()].findAll{ 
            it.length() == len && it.substring(1).equals(end)
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
        start = start.toUpperCase()
        def firstLetter = start.getAt(0)
        def len = start.length() + 1
        def result = wordsByLetter[firstLetter][0].findAll{ 
            it.length() == len && it.startsWith(start)
        }.collect{
            it.getAt(it.length()-1)
        };
        return result
    }
    
    def validSingleChar(prefix, suffix){
        prefix = prefix.toUpperCase()
        suffix = suffix.toUpperCase()
        def len = prefix.length() + suffix.length() + 1
        def firstLetter = prefix.getAt(0)
        def result = wordsByLetter[firstLetter][0].findAll{ 
            it.length() == len && it.startsWith(prefix) && it.endsWith(suffix)
        }.collect{
            it.getAt(prefix.length())
        };
        return result
    }
}

