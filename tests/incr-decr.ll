int i = 5;
print i;    //5
↑i 
print i     //6
int j = ↑i+1
print j     //8

//predecrements
↓i
print i     //6
j = ↓i+1
print j     //6

//post
i↑
print i     //6
j↓
print j     //5
j = i↓ + 1
print j     //7
print ↑i   //6

print "Beginning assignments test!"
int g = 3;
print g     //3
g += 5
print g     //8
g -= 5
print g     //3
g *= 4      
print g     //12
g /= 6
print g     //2
g %= 10
print g     //2
g *= 12
g %= 10
print g     //4

string txt = "Hello"
txt += " world!"
print txt
