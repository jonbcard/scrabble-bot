package org.card.scrabble
// Random testing

Board b = new Board()

// -------------- Test Coordinates -----------
def coords 

coords = b.getDirectionAndCoords("A1")
assert coords.dir == b.cols
assert coords.coord1 == 0
assert coords.coord2 == 0

coords = b.getDirectionAndCoords("1A")
assert coords.dir == b.rows
assert coords.coord1 == 0
assert coords.coord2 == 0


coords = b.getDirectionAndCoords("4B")
assert coords.dir == b.rows
assert coords.coord1 == 3
assert coords.coord2 == 1

coords = b.getDirectionAndCoords("B4")
assert coords.dir == b.cols
assert coords.coord1 == 1
assert coords.coord2 == 3

// -------------- Test Square methods -----------
b.playMove("PLAY: 1 8H CAD 3")
b.playMove("PLAY: 1 9I DONE 3")
b.playMove("PLAY: 1 L9 EAT 3")
b.playMove("PLAY: 1 11L TINE 3")
b.playMove("PLAY: 1 O11 EARTH 3")
b.playMove("PLAY: 1 A1 DOG 3")

Square s = b.rows[9][10]
// assert s.getBlankAdjacent().size() == 2
assert s.getLettersAbove() == "N"
assert s.getLettersBelow() == ""
assert s.getLettersToLeft() == ""
assert s.getLettersToRight() == "A"
assert s.rowAddons == ['A','E','O','U']
assert s.columnAddons == ['A', 'B', 'F', 'H', 'K', 'L', 'M', 'N', 'P', 'T', 'Y', 'Z']

s = b.rows[9][14]
// assert s.getBlankAdjacent().size() == 2
assert s.getLettersAbove() == ""
assert s.getLettersToLeft() == ""
assert s.getLettersToRight() == ""
assert s.getLettersBelow() == "EARTH"
assert s.rowAddons == ['D', 'H']
assert s.columnAddons == null

assert b.rows[8][12].getLettersToLeft() == "DONE"
assert b.rows[3][0].getLettersAbove() == "DOG"
assert b.rows[6][9].getLettersBelow() == "DO"
assert b.rows[0][1].columnAddons == ['E', 'O']
assert b.rows[0][1].rowAddons == null



// -------------- Test Scoring -----------

assert new Play(b, b.rows, 1, 0, "ODD").scorePlay() == 10
assert new Play(b, b.rows, 3, 0, "YEARN").scorePlay() == 37
assert new Play(b, b.rows, 9, 10, "AA").scorePlay() == 4
assert new Play(b, b.cols, 1, 1, "NOD").scorePlay() == 15

// -------------- Test Anchors, and Space Counts ----------- 
assert b.getRowAnchors(0).collect{ it.colPos } == [0]
assert b.getRowAnchors(3).collect{ it.colPos } == [0]
assert b.getRowAnchors(4).collect{it.colPos} == []
assert b.getRowAnchors(7).collect{it.colPos} == [7, 11]
assert b.getRowAnchors(9).collect{it.colPos} == [8, 9, 10, 11, 13, 14]
assert b.getRowAnchors(14).collect{it.colPos} == []

assert b.getColumnAnchors(0).collect{ it.rowPos } == [0]
assert b.getColumnAnchors(1).collect{ it.rowPos } == [0, 1, 2]
assert b.getColumnAnchors(7).collect{ it.rowPos } == [7]
assert b.getColumnAnchors(14).collect{ it.rowPos } == [10]

assert b.rows[0][0].getLeftSpace() == 0
assert b.rows[0][5].getLeftSpace() == 3
assert b.rows[7][7].getLeftSpace() == 7
assert b.rows[7][11].getLeftSpace() == 0
assert b.rows[7][12].getLeftSpace() == 0
assert b.rows[7][13].getLeftSpace() == 1
assert b.rows[3][12].getLeftSpace() == 6

assert b.rows[1][1].getTopSpace() == 0
assert b.rows[7][9].getTopSpace() == 7
assert b.rows[8][10].getTopSpace() == 0
assert b.rows[12][8].getTopSpace() == 2
assert b.rows[13][8].getTopSpace() == 3

assert b.rows[7][5].getRowIllegalLengths() == [2,3,4]

// -------------- Test Lexicon -----------
Lexicon l = Lexicon.getInstance()
assert l.getStartingSet(10,'Q') == ["MICROEARTHQUAKE", "SCULPTURESQUE", "SCULPTURESQUELY", "UNPICTURESQUE"]
assert l.getStartingSet(10,['Q','X']).size() == 18
assert l.getAllMatches([0:'C',1:'A', 2:['F','L']], [], 5, "BARFELY".toList()) == ['(C)(A)FE', '(C)(A)LF']
assert l.getAllMatches([0:['C','X','Y','Z','Q','D','G','H'],1:'A', 2:['F','L']], [], 5, "BBRFELC".toList()) == ['C(A)FE', 'C(A)LF']
assert l.getAllMatches([0:['C','X','Y','Z','Q','D','G','H'],1:'A', 2:['F','L']], [], 5, "BHRFELC".toList()) == ['C(A)FE', 'C(A)LF', 'H(A)', 'H(A)LE', 'H(A)LER', 'H(A)LF']    
assert l.getAllMatches([0:['C','D','G','H'],1:'A', 2:['F','L']], [], 4, "BHRFELC".toList()) == ['C(A)FE', 'C(A)LF', 'H(A)', 'H(A)LE', 'H(A)LF']  
assert l.getAllMatches([0:['D','G'],1:'A', 2:['F','L']], [], 3, "BHR*ELC".toList()) == ['d(A)L', 'g(A)L']
assert l.getAllMatches([0:['D','G'],1:'[]', 2:['F','L']], [], 3, "DGFLAE*".toList()) == []
assert l.wordIsValid([3:'EIS'.toList(), 4:'N', 5:'ABFHKLMNPTYZ'.toList(), 6:[]], [], 7, "ACEDE*Z".toList(), "CADENZA") == null

// -------------- Test Rack -----------
assert b.rows[0][0].value == "D"
assert b.rows[0][1].value == null

Rack r = new Rack(b)
r.applyRack("TILES: A C E D E * Z")
def bestPlay = r.buildPlays()
println bestPlay.playString()

// Test cases taken from harness failures
b = new Board()
b.playMove("PLAY: 0 8A WHAP 45")
b.playMove("PLAY: 1 E8 SOONER 25")
b.playMove("PLAY: 0 H1 FUNK 48")
b.playMove("PLAY: 1 D10 LUBE 26")
b.playMove("PLAY: 0 5C CRAGGY 37")
assert b.rows[10][5].columnAddons == ['S']

b = new Board()
r = new Rack(b)
b.playMove("PLAY: 0 8H DECAY 26")
b.playMove("PLAY: 1 H8 (D)OUZEPER 113")
r.applyRack("TILES: R E Y H I X K")
bestPlay = r.buildPlays()
assert bestPlay.playString() == "I7 K(E)X"