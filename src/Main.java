import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final String NAMES = "ABC";

    private static final String MENU =
            "\n 1 - ввести матрицу        2 - единичная матрица\n" +
            " 3 - X + Y    4 - X - Y    5 - X * Y    6 - X / Y\n" +
            " 7 - X * число             8 - степень X^p\n" +
            " 9 - транспонировать      10 - эрмитово сопряжение\n" +
            "11 - обратная             12 - определитель      13 - след\n" +
            "14 - показать все          0 - выход";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        in.useLocale(Locale.US);
        Matrix[] slots = new Matrix[NAMES.length()];
        boolean running = true;

        while (running) {
            System.out.println(MENU);
            System.out.print("> ");
            try {
                int command = in.nextInt();
                switch (command) {
                    case 1: {
                        int target = readSlot(in, "Какую матрицу ввести");
                        slots[target] = readMatrix(in);
                        break;
                    }
                    case 2: {
                        System.out.print("Размер n: ");
                        save(in, slots, Matrix.identity(in.nextInt()));
                        break;
                    }
                    case 3: {
                        Matrix x = pick(in, slots, "Первая матрица");
                        Matrix y = pick(in, slots, "Вторая матрица");
                        save(in, slots, x.add(y));
                        break;
                    }
                    case 4: {
                        Matrix x = pick(in, slots, "Первая матрица");
                        Matrix y = pick(in, slots, "Вторая матрица");
                        save(in, slots, x.sub(y));
                        break;
                    }
                    case 5: {
                        Matrix x = pick(in, slots, "Первая матрица");
                        Matrix y = pick(in, slots, "Вторая матрица");
                        save(in, slots, x.multiply(y));
                        break;
                    }
                    case 6: {
                        Matrix x = pick(in, slots, "Делимое");
                        Matrix y = pick(in, slots, "Делитель");
                        save(in, slots, x.divide(y));
                        break;
                    }
                    case 7: {
                        Matrix x = pick(in, slots, "Матрица");
                        System.out.print("Число (например 2, -i, 1+2i): ");
                        save(in, slots, x.scale(Complex.parse(in.next())));
                        break;
                    }
                    case 8: {
                        Matrix x = pick(in, slots, "Матрица");
                        System.out.print("Степень p (целое, можно отрицательное): ");
                        save(in, slots, x.power(in.nextInt()));
                        break;
                    }
                    case 9: {
                        Matrix x = pick(in, slots, "Матрица");
                        save(in, slots, x.transpose());
                        break;
                    }
                    case 10: {
                        Matrix x = pick(in, slots, "Матрица");
                        save(in, slots, x.conjugateTranspose());
                        break;
                    }
                    case 11: {
                        Matrix x = pick(in, slots, "Матрица");
                        save(in, slots, x.inverse());
                        break;
                    }
                    case 12: {
                        Matrix x = pick(in, slots, "Матрица");
                        System.out.println("Определитель: " + x.determinant());
                        break;
                    }
                    case 13: {
                        Matrix x = pick(in, slots, "Матрица");
                        System.out.println("След: " + x.trace());
                        break;
                    }
                    case 14: {
                        for (int i = 0; i < slots.length; i++) {
                            System.out.println(NAMES.charAt(i) + ":");
                            if (slots[i] == null) {
                                System.out.println("не задана");
                            } else {
                                System.out.println(slots[i].toString());
                            }
                        }
                        break;
                    }
                    case 0: {
                        running = false;
                        break;
                    }
                    default: {
                        System.out.println("Неизвестная команда");
                    }
                }
            } catch (MatrixSizeException | ArithmeticException | IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Ожидалось целое число");
                in.nextLine();
            } catch (NoSuchElementException e) {
                System.out.println("Ввод закончен");
                running = false;
            }
        }
    }

    private static int slotIndex(String name) {
        if (name.length() != 1) {
            return -1;
        }
        return NAMES.indexOf(name.toUpperCase());
    }

    private static int readSlot(Scanner in, String prompt) {
        System.out.print(prompt + " (A/B/C): ");
        String name = in.next();
        int index = slotIndex(name);
        if (index < 0) {
            throw new IllegalArgumentException("Нет матрицы " + name);
        }
        return index;
    }

    private static Matrix pick(Scanner in, Matrix[] slots, String prompt) {
        int index = readSlot(in, prompt);
        if (slots[index] == null) {
            throw new IllegalArgumentException("Матрица " + NAMES.charAt(index) + " не задана");
        }
        return slots[index];
    }

    private static void save(Scanner in, Matrix[] slots, Matrix result) {
        System.out.println("Результат:");
        System.out.println(result);
        System.out.print("Сохранить в (A/B/C, - не сохранять): ");
        String name = in.next();
        if (name.equals("-")) {
            return;
        }
        int index = slotIndex(name);
        if (index < 0) {
            System.out.println("Нет матрицы " + name + ", результат не сохранён");
            return;
        }
        slots[index] = result;
        System.out.println("Сохранено в " + NAMES.charAt(index));
    }

    private static Matrix readMatrix(Scanner in) {
        System.out.print("Строк: ");
        int rows = in.nextInt();
        System.out.print("Столбцов: ");
        int cols = in.nextInt();
        Matrix m = new Matrix(rows, cols);
        System.out.println("Элементы вводятся без пробелов внутри числа: 1+2i, 3-i, -2.5, 4i, i");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print("Элемент [" + i + "][" + j + "]: ");
                m.set(i, j, Complex.parse(in.next()));
            }
        }
        return m;
    }
}
