public class DateTimeParserTokenManager implements DateTimeParserConstants
{
        static int commentNest;
  public  java.io.PrintStream debugStream = System.out;
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x7fe7cf7f0L) != 0L)
         {
            jjmatchedKind = 35;
            return -1;
         }
         return -1;
      case 1:
         if ((active0 & 0x7fe7cf7f0L) != 0L)
         {
            if (jjmatchedPos == 0)
            {
               jjmatchedKind = 35;
               jjmatchedPos = 0;
            }
            return -1;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private final int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private final int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 10:
         return jjStopAtPos(0, 2);
      case 13:
         return jjStopAtPos(0, 1);
      case 40:
         return jjStopAtPos(0, 37);
      case 44:
         return jjStopAtPos(0, 3);
      case 58:
         return jjStopAtPos(0, 23);
      case 65:
         return jjMoveStringLiteralDfa1_0(0x44000L);
      case 67:
         return jjMoveStringLiteralDfa1_0(0x60000000L);
      case 68:
         return jjMoveStringLiteralDfa1_0(0x400000L);
      case 69:
         return jjMoveStringLiteralDfa1_0(0x18000000L);
      case 70:
         return jjMoveStringLiteralDfa1_0(0x1100L);
      case 71:
         return jjMoveStringLiteralDfa1_0(0x4000000L);
      case 74:
         return jjMoveStringLiteralDfa1_0(0x30800L);
      case 77:
         return jjMoveStringLiteralDfa1_0(0x18000a010L);
      case 78:
         return jjMoveStringLiteralDfa1_0(0x200000L);
      case 79:
         return jjMoveStringLiteralDfa1_0(0x100000L);
      case 80:
         return jjMoveStringLiteralDfa1_0(0x600000000L);
      case 83:
         return jjMoveStringLiteralDfa1_0(0x80600L);
      case 84:
         return jjMoveStringLiteralDfa1_0(0xa0L);
      case 85:
         return jjMoveStringLiteralDfa1_0(0x2000000L);
      case 87:
         return jjMoveStringLiteralDfa1_0(0x40L);
      default :
         return jjMoveNfa_0(0, 0);
   }
}
private final int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 68:
         return jjMoveStringLiteralDfa2_0(active0, 0x550000000L);
      case 77:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000000L);
      case 83:
         return jjMoveStringLiteralDfa2_0(active0, 0x2a8000000L);
      case 84:
         if ((active0 & 0x2000000L) != 0L)
            return jjStopAtPos(1, 25);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0xaa00L);
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0x100000L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x481040L);
      case 104:
         return jjMoveStringLiteralDfa2_0(active0, 0x80L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x200010L);
      case 112:
         return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x100L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x70420L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0); 
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 84:
         if ((active0 & 0x4000000L) != 0L)
            return jjStopAtPos(2, 26);
         else if ((active0 & 0x8000000L) != 0L)
            return jjStopAtPos(2, 27);
         else if ((active0 & 0x10000000L) != 0L)
            return jjStopAtPos(2, 28);
         else if ((active0 & 0x20000000L) != 0L)
            return jjStopAtPos(2, 29);
         else if ((active0 & 0x40000000L) != 0L)
            return jjStopAtPos(2, 30);
         else if ((active0 & 0x80000000L) != 0L)
            return jjStopAtPos(2, 31);
         else if ((active0 & 0x100000000L) != 0L)
            return jjStopAtPos(2, 32);
         else if ((active0 & 0x200000000L) != 0L)
            return jjStopAtPos(2, 33);
         else if ((active0 & 0x400000000L) != 0L)
            return jjStopAtPos(2, 34);
         break;
      case 98:
         if ((active0 & 0x1000L) != 0L)
            return jjStopAtPos(2, 12);
         break;
      case 99:
         if ((active0 & 0x400000L) != 0L)
            return jjStopAtPos(2, 22);
         break;
      case 100:
         if ((active0 & 0x40L) != 0L)
            return jjStopAtPos(2, 6);
         break;
      case 101:
         if ((active0 & 0x20L) != 0L)
            return jjStopAtPos(2, 5);
         break;
      case 103:
         if ((active0 & 0x40000L) != 0L)
            return jjStopAtPos(2, 18);
         break;
      case 105:
         if ((active0 & 0x100L) != 0L)
            return jjStopAtPos(2, 8);
         break;
      case 108:
         if ((active0 & 0x20000L) != 0L)
            return jjStopAtPos(2, 17);
         break;
      case 110:
         if ((active0 & 0x10L) != 0L)
            return jjStopAtPos(2, 4);
         else if ((active0 & 0x400L) != 0L)
            return jjStopAtPos(2, 10);
         else if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(2, 11);
         else if ((active0 & 0x10000L) != 0L)
            return jjStopAtPos(2, 16);
         break;
      case 112:
         if ((active0 & 0x80000L) != 0L)
            return jjStopAtPos(2, 19);
         break;
      case 114:
         if ((active0 & 0x2000L) != 0L)
            return jjStopAtPos(2, 13);
         else if ((active0 & 0x4000L) != 0L)
            return jjStopAtPos(2, 14);
         break;
      case 116:
         if ((active0 & 0x200L) != 0L)
            return jjStopAtPos(2, 9);
         else if ((active0 & 0x100000L) != 0L)
            return jjStopAtPos(2, 20);
         break;
      case 117:
         if ((active0 & 0x80L) != 0L)
            return jjStopAtPos(2, 7);
         break;
      case 118:
         if ((active0 & 0x200000L) != 0L)
            return jjStopAtPos(2, 21);
         break;
      case 121:
         if ((active0 & 0x8000L) != 0L)
            return jjStopAtPos(2, 15);
         break;
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 4;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 46)
                        kind = 46;
                     jjCheckNAdd(3);
                  }
                  else if ((0x100000200L & l) != 0L)
                  {
                     if (kind > 36)
                        kind = 36;
                     jjCheckNAdd(2);
                  }
                  else if ((0x280000000000L & l) != 0L)
                  {
                     if (kind > 24)
                        kind = 24;
                  }
                  break;
               case 2:
                  if ((0x100000200L & l) == 0L)
                     break;
                  kind = 36;
                  jjCheckNAdd(2);
                  break;
               case 3:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  kind = 46;
                  jjCheckNAdd(3);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffbfe07fffbfeL & l) != 0L)
                     kind = 35;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 4 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_1(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_1(int pos, long active0)
{
   return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_1(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_1(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 40);
      case 41:
         return jjStopAtPos(0, 38);
      default :
         return jjMoveNfa_1(0, 0);
   }
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private final int jjMoveNfa_1(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (kind > 41)
                     kind = 41;
                  break;
               case 1:
                  if (kind > 39)
                     kind = 39;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (kind > 41)
                     kind = 41;
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (kind > 39)
                     kind = 39;
                  break;
               case 2:
                  if (kind > 41)
                     kind = 41;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 41)
                     kind = 41;
                  break;
               case 1:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 39)
                     kind = 39;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_2(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_2(int pos, long active0)
{
   return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
}
private final int jjStartNfaWithStates_2(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_2(state, pos + 1);
}
private final int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 40:
         return jjStopAtPos(0, 43);
      case 41:
         return jjStopAtPos(0, 44);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private final int jjMoveNfa_2(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 3;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (kind > 45)
                     kind = 45;
                  break;
               case 1:
                  if (kind > 42)
                     kind = 42;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if (kind > 45)
                     kind = 45;
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 1;
                  break;
               case 1:
                  if (kind > 42)
                     kind = 42;
                  break;
               case 2:
                  if (kind > 45)
                     kind = 45;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 45)
                     kind = 45;
                  break;
               case 1:
                  if ((jjbitVec0[i2] & l2) != 0L && kind > 42)
                     kind = 42;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 3 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
};
public static final String[] jjstrLiteralImages = {
"", "\15", "\12", "\54", "\115\157\156", "\124\165\145", "\127\145\144", 
"\124\150\165", "\106\162\151", "\123\141\164", "\123\165\156", "\112\141\156", 
"\106\145\142", "\115\141\162", "\101\160\162", "\115\141\171", "\112\165\156", 
"\112\165\154", "\101\165\147", "\123\145\160", "\117\143\164", "\116\157\166", 
"\104\145\143", "\72", null, "\125\124", "\107\115\124", "\105\123\124", "\105\104\124", 
"\103\123\124", "\103\104\124", "\115\123\124", "\115\104\124", "\120\123\124", 
"\120\104\124", null, null, null, null, null, null, null, null, null, null, null, null, null, 
null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
   "INCOMMENT", 
   "NESTED_COMMENT", 
};
public static final int[] jjnewLexState = {
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x400fffffffffL, 
};
static final long[] jjtoSkip = {
   0x5000000000L, 
};
static final long[] jjtoSpecial = {
   0x1000000000L, 
};
static final long[] jjtoMore = {
   0x3fa000000000L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[4];
private final int[] jjstateSet = new int[8];
StringBuffer image;
int jjimageLen;
int lengthOfMatch;
protected char curChar;
public DateTimeParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}
public DateTimeParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 4; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 3 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}
protected Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}
int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;
public Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;
  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }
   image = null;
   jjimageLen = 0;
   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         break;
       case 2:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
           matchedToken.specialToken = specialToken;
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
           {
              matchedToken = jjFillToken();
              if (specialToken == null)
                 specialToken = matchedToken;
              else
              {
                 matchedToken.specialToken = specialToken;
                 specialToken = (specialToken.next = matchedToken);
              }
           }
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
        MoreLexicalActions();
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}
void MoreLexicalActions()
{
   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
   switch(jjmatchedKind)
   {
      case 39 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
                          image.deleteCharAt(image.length() - 2);
         break;
      case 40 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
              commentNest = 1;
         break;
      case 42 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
                          image.deleteCharAt(image.length() - 2);
         break;
      case 43 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
              ++commentNest;
         break;
      case 44 :
         if (image == null)
            image = new StringBuffer();
         image.append(input_stream.GetSuffix(jjimageLen));
         jjimageLen = 0;
              --commentNest; if (commentNest == 0) SwitchTo(INCOMMENT);
         break;
      default : 
         break;
   }
}
}
