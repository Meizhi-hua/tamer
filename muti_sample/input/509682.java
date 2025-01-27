public abstract class DTMAxisTraverser
{
  public int first(int context)
  {
    return next(context, context);
  }
  public int first(int context, int extendedTypeID)
  {
    return next(context, context, extendedTypeID);
  }
  public abstract int next(int context, int current);
  public abstract int next(int context, int current, int extendedTypeID);
}
