public class Matrix {
    private static final double EPS = 1e-24;

    private final int rows;
    private final int cols;
    private final Complex[][] data;

    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Размеры матрицы должны быть положительными");
        }
        this.rows = rows;
        this.cols = cols;
        this.data = new Complex[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = Complex.ZERO;
            }
        }
    }

    public static Matrix identity(int n) {
        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            result.data[i][i] = Complex.ONE;
        }
        return result;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Complex get(int i, int j) {
        checkIndex(i, j);
        return data[i][j];
    }

    public void set(int i, int j, Complex value) {
        checkIndex(i, j);
        if (value == null) {
            throw new IllegalArgumentException("Элемент матрицы не может быть null");
        }
        data[i][j] = value;
    }

    public Matrix add(Matrix other) {
        checkSameSize(other, "Складывать");
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j].add(other.data[i][j]);
            }
        }
        return result;
    }

    public Matrix sub(Matrix other) {
        checkSameSize(other, "Вычитать");
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j].sub(other.data[i][j]);
            }
        }
        return result;
    }

    public Matrix multiply(Matrix other) {
        if (cols != other.rows) {
            throw new MatrixSizeException("Число столбцов первой матрицы должно равняться числу строк второй");
        }
        Matrix result = new Matrix(rows, other.cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < other.cols; j++) {
                Complex sum = Complex.ZERO;
                for (int k = 0; k < cols; k++) {
                    sum = sum.add(data[i][k].mul(other.data[k][j]));
                }
                result.data[i][j] = sum;
            }
        }
        return result;
    }

    public Matrix scale(Complex factor) {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[i][j] = data[i][j].mul(factor);
            }
        }
        return result;
    }

    public Matrix power(int p) {
        checkSquare("Возводить в степень можно только квадратную матрицу");
        if (p < 0) {
            return inverse().power(-p);
        }
        Matrix result = identity(rows);
        for (int step = 0; step < p; step++) {
            result = result.multiply(this);
        }
        return result;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = data[i][j];
            }
        }
        return result;
    }

    public Matrix conjugateTranspose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = data[i][j].conjugate();
            }
        }
        return result;
    }

    public Complex trace() {
        checkSquare("След есть только у квадратной матрицы");
        Complex sum = Complex.ZERO;
        for (int i = 0; i < rows; i++) {
            sum = sum.add(data[i][i]);
        }
        return sum;
    }

    public Complex determinant() {
        checkSquare("Определитель есть только у квадратной матрицы");
        int n = rows;
        Complex[][] m = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = data[i][j];
            }
        }
        Complex det = Complex.ONE;
        for (int k = 0; k < n; k++) {
            int pivotRow = findPivotRow(m, k);
            if (m[pivotRow][k].norm() < EPS) {
                return Complex.ZERO;
            }
            if (pivotRow != k) {
                swapRows(m, pivotRow, k);
                det = det.negate();
            }
            det = det.mul(m[k][k]);
            for (int i = k + 1; i < n; i++) {
                Complex factor = m[i][k].div(m[k][k]);
                for (int j = k; j < n; j++) {
                    m[i][j] = m[i][j].sub(factor.mul(m[k][j]));
                }
            }
        }
        return det;
    }

    public Matrix inverse() {
        checkSquare("Обратная матрица есть только у квадратной матрицы");
        int n = rows;
        Complex[][] m = new Complex[n][2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = data[i][j];
            }
            for (int j = n; j < 2 * n; j++) {
                m[i][j] = Complex.ZERO;
            }
            m[i][n + i] = Complex.ONE;
        }

        for (int k = 0; k < n; k++) {
            int pivotRow = findPivotRow(m, k);
            if (m[pivotRow][k].norm() < EPS) {
                throw new ArithmeticException("Матрица вырожденная, обратной нет");
            }
            swapRows(m, pivotRow, k);
            Complex pivot = m[k][k];
            for (int j = 0; j < 2 * n; j++) {
                m[k][j] = m[k][j].div(pivot);
            }
            for (int i = 0; i < n; i++) {
                if (i != k) {
                    Complex factor = m[i][k];
                    for (int j = k; j < 2 * n; j++) {
                        m[i][j] = m[i][j].sub(factor.mul(m[k][j]));
                    }
                }
            }
        }

        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result.data[i][j] = m[i][n + j];
            }
        }
        return result;
    }

    public Matrix divide(Matrix other) {
        if (other.rows != other.cols) {
            throw new MatrixSizeException("Делитель должен быть квадратной матрицей");
        }
        if (cols != other.rows) {
            throw new MatrixSizeException("Число столбцов делимого должно равняться размеру делителя");
        }
        return multiply(other.inverse());
    }

    private static int findPivotRow(Complex[][] m, int k) {
        int best = k;
        for (int i = k + 1; i < m.length; i++) {
            if (m[i][k].norm() > m[best][k].norm()) {
                best = i;
            }
        }
        return best;
    }

    private static void swapRows(Complex[][] m, int a, int b) {
        Complex[] tmp = m[a];
        m[a] = m[b];
        m[b] = tmp;
    }

    private void checkIndex(int i, int j) {
        if (i < 0 || i >= rows || j < 0 || j >= cols) {
            throw new IndexOutOfBoundsException("Нет элемента [" + i + "][" + j + "] в матрице "
                    + rows + "x" + cols);
        }
    }

    private void checkSameSize(Matrix other, String action) {
        if (rows != other.rows || cols != other.cols) {
            throw new MatrixSizeException(action + " можно только матрицы одного размера");
        }
    }

    private void checkSquare(String message) {
        if (rows != cols) {
            throw new MatrixSizeException(message);
        }
    }

    @Override
    public String toString() {
        String[][] cells = new String[rows][cols];
        int width = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = data[i][j].toString();
                if (cells[i][j].length() > width) {
                    width = cells[i][j].length();
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append("| ");
            for (int j = 0; j < cols; j++) {
                sb.append(String.format("%-" + width + "s", cells[i][j]));
                if (j < cols - 1) {
                    sb.append("   ");
                } else {
                    sb.append(" |");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
