//bool cond1

def a():
    def b():
        print "in b"
        c()
        def c():
            print "in c"
            if true: //cond1:
                int expectedValue = 5;
                def d() returns int:
                    print "in d (1)"
                    e()
                    return 5
                if d() != expectedValue:
                    print "FAIL"
            else:
                def d():
                    print "in d (2)"
                    e()
                d()
            print "out d"
        print "out c"
    def e():
        print "here is e"
    print "in a"
    b()
    print "out b"
    print "out a"

//cond1 = true
a()

// FIXME: implementar globals, i moure això a un altre test nou!
/*print ""
cond1 = false
a()*/
