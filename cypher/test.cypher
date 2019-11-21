LOAD CSV WITH HEADERS FROM '/home/hacker/test/data.csv' AS data
CREATE (st {name:data.st})
CREATE (ot {name:data.ot})
CREATE (st)-[:r{name:data.p}]->(ot)
return *
