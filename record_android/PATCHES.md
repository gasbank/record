# Local recording input safety patch

Based on gasbank/record revision 1c8daf95d6ecc216bf871bfa749e2db90f2f6c8d.

- Require immediate audio focus before starting the recording thread.
- Reject delayed/failed focus requests instead of recording without focus.
- Surface Android capture-policy silencing as an input error.

The application handles these signals through its shared recording session.
