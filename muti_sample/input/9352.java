public class NoSuchSymbolException extends RuntimeException {
  private String symbol;
  public NoSuchSymbolException(String symbol) {
    super();
    this.symbol = symbol;
  }
  public NoSuchSymbolException(String symbol, Throwable cause) {
    super(cause);
    this.symbol = symbol;
  }
  public NoSuchSymbolException(String symbol, String detail) {
    super(detail);
    this.symbol = symbol;
  }
  public NoSuchSymbolException(String symbol, String detail, Throwable cause) {
    super(detail, cause);
    this.symbol = symbol;
  }
  public String getSymbol() {
    return symbol;
  }
}
