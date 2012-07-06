default touch S2

if isTouching(S1):
    print "TOUCHING 1"
if isTouching(S2) :
    print "TOUCHING 2"
setLamp(RED, S2)
int dist = rangeScan(50, S2)
