
# Pollster service

Restful api sopporting privacy preserving verifiable
elections

# How to Build and run

Build with the following:

    sbt package

Run assembled jar file using java -jar 

# Polls and Ballots
The polling mechanism uses el-gamal encryption along with non interactive
zero knowledge proofs in ballots to establish the following properties:

1. Ballots have encrypted ballots and have voter identity in the clear  Everyone can see
and verify these ballots.  The ballots in their clear form would indicate a yay or a nay vote
but the vote can not be deciphered from the encrypted ballot. 

2. At end of the poll (no more votes can be cast) the service produces a total transcript of the
poll at which time, the following can be verified by anyone
   a. The encrypted total is correct using modular arithmetic
   b. The decryped value is the total number of positive votes

More details in the white paper (TBP).  The mechanism is quite similar to that in the following
: https://www.win.tue.nl/~berry/papers/euro97.pdf . Zero knowledge additions for 2b are similar
to the question asked here: https://crypto.stackexchange.com/questions/9997/perfect-zero-knowledge-for-the-schnorr-protocol

### Ballot sketch
![](./docs/ballot-spec.png "Ballot")
