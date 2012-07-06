// INT (constants)
print 1 == 1
print 1 == 2
print ""

// INT (variables)
int i1 = 0
int i2 = 5
int i3 = i1 + 3
int i4 = i2 + 7
print i1 == i2
print i2 == i4
print i4 == 12
print ""

// FLOAT
print 41.0 == 41.0
print 41.1 == 41.2
print 42.0 == 64000.0
print ""

// BOOL
print true == true
print false == false
print true == false
print false == true
print ""

// STRING (constants)
print "" == ""
print "a" == "a"
print "hello world!" == "hello world!"
print "" == "f"
print "a" == "f"
print "a" == "fuh"
print "meh" == "mah"
print ""

// STRING (variables)
// (Warning: the compiler optimizes many of these away)
string s1 = ""
string s2 = ""
string s3 = "a"
string s4 = "hello world!"
string s5 = "hello world!"
string s6 = "Hello world!" // capitalized
string s7 = "hello"
print s1 == s2
print s1 == ""
print s1 == s3
print s3 == "a"
print s4 == s4
print s4 == s5
print s5 == s4
print s5 == s6
print "Hello world!" == s6
print (s4 + s5) == (s5 + s4)
print ("hello" + s3) == (s3 + "hello")
print ("hello" + s3) == (s7 + s3)
print ""

// ARRAY (int)
int[] a
int[] b
int[] c
a[] = 1
a[] = 2
b[] = 1
b[] = 2
c[] = 1
c[] = 3
print a[0] == a[0]
print a[0] == b[0]
print a[0] == c[0]
print a[1] == b[1]
print a[1] == c[1]
print a == a
print a == b
print a == c
print ""

// COLOR
//print RED == RED
//print GREEN == BLUE
//print ""

// FUNCTIONS
def f21() returns int:
    return 21
def f42() returns int:
    return 42
def fX(int x) returns int:
    return x
print f21() == f42()
print fX(50) == fX(50)
print fX(42) == f42()
