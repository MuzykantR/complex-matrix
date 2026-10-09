public final class Complex {
    public static final Complex ZERO = new Complex(0, 0);
    public static final Complex ONE = new Complex(1, 0);

    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double getRe() {
        return re;
    }

    public double getIm() {
        return im;
    }

    public Complex add(Complex other) {
        return new Complex(re + other.re, im + other.im);
    }

    public Complex sub(Complex other) {
        return new Complex(re - other.re, im - other.im);
    }

    public Complex mul(Complex other) {
        double realPart = re * other.re - im * other.im;
        double imagPart = re * other.im + im * other.re;
        return new Complex(realPart, imagPart);
    }

    public Complex div(Complex other) {
        double denominator = other.norm();
        if (denominator == 0) {
            throw new ArithmeticException("Деление на комплексный ноль");
        }
        double realPart = (re * other.re + im * other.im) / denominator;
        double imagPart = (im * other.re - re * other.im) / denominator;
        return new Complex(realPart, imagPart);
    }

    public Complex negate() {
        return new Complex(-re, -im);
    }

    public Complex conjugate() {
        return new Complex(re, -im);
    }

    public double norm() {
        return re * re + im * im;
    }

    public static Complex parse(String text) {
        String s = text.trim().toLowerCase();
        try {
            if (!s.endsWith("i")) {
                return new Complex(Double.parseDouble(s), 0);
            }
            String body = s.substring(0, s.length() - 1);
            int split = -1;
            for (int pos = body.length() - 1; pos > 0; pos--) {
                char c = body.charAt(pos);
                if ((c == '+' || c == '-') && body.charAt(pos - 1) != 'e') {
                    split = pos;
                    break;
                }
            }
            double realPart = 0;
            String imagText = body;
            if (split > 0) {
                realPart = Double.parseDouble(body.substring(0, split));
                imagText = body.substring(split);
            }
            return new Complex(realPart, parseImaginary(imagText));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Не удалось прочитать число: " + text);
        }
    }

    private static double parseImaginary(String text) {
        if (text.isEmpty() || text.equals("+")) {
            return 1;
        }
        if (text.equals("-")) {
            return -1;
        }
        return Double.parseDouble(text);
    }

    private static String format(double x) {
        double rounded = Math.round(x * 10000.0) / 10000.0;
        if (rounded == (long) rounded) {
            return Long.toString((long) rounded);
        }
        return Double.toString(rounded);
    }

    @Override
    public String toString() {
        String imagText = format(Math.abs(im));
        String sign;
        if (im < 0 && !imagText.equals("0")) {
            sign = " - ";
        } else {
            sign = " + ";
        }
        return format(re) + sign + imagText + "i";
    }
}
