for i in 1 .. 15:
    print i
    if i % 10 == 6:
        break

print ""

string[] xs
xs[] = "aa"
xs[] = "bb"
xs[] = "cc"

foreach x in xs:
    print x
    foreach y in xs:
        for i in 3 .. 30:
            print y
            break
        break

print ""
print "out"
