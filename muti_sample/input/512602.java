@TypeQualifier(applicableTo = Number.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Nonnegative {
    When when() default When.ALWAYS;
    class Checker implements TypeQualifierValidator<Nonnegative> {
        public When forConstantValue(Nonnegative annotation, Object v) {
            if (!(v instanceof Number))
                return When.NEVER;
            boolean isNegative;
            Number value = (Number) v;
            if (value instanceof Long)
                isNegative = value.longValue() < 0;
            else if (value instanceof Double)
                isNegative = value.doubleValue() < 0;
            else if (value instanceof Float)
                isNegative = value.floatValue() < 0;
            else
                isNegative = value.intValue() < 0;
            if (isNegative)
                return When.NEVER;
            else
                return When.ALWAYS;
        }
    }
}
