def giveMeAFloat() returns float:
    int x = 7
    return x

print giveMeAFloat()

def giveMeAnInt() returns int:
    float x = 5.3
    return x

print giveMeAnInt()

// Floats are rounded when being cast to an integer
float fRoundUp = 5.7
int f = fRoundUp
print f
