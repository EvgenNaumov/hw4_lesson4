public class LettersSample {
    private volatile char currentLetter = 'A';

    public static void main(String[] args) {
        LettersSample lookLetters = new LettersSample();

        Thread letterA = new Thread(()->lookLetters.metodA());
        Thread letterB = new Thread(()->lookLetters.metodB());
        Thread letterC = new Thread(()->lookLetters.metodC());

        letterA.setName("metod A ->");
        letterB.setName("metod B ->");
        letterC.setName("metod C ->");

        letterA.start();
        letterB.start();
        letterC.start();

    }

    private synchronized void metodC() {
        try {
            synchronized (this) {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter!='C'){
                        wait();
                    }
                    PrintLetter('C');
                    currentLetter = 'A';
                    notifyAll();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void metodB() {
        try {
            synchronized (this) {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter!='B'){
                        wait();
                    }
                    PrintLetter('B');
                    currentLetter = 'C';
                    notifyAll();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void metodA() {
        try {
            synchronized (this) {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter!='A'){
                        wait();
                    }
                    PrintLetter('A');
                    currentLetter = 'B';
                    notifyAll();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void PrintLetter(char l){
//        System.out.println(Thread.currentThread().getName() +  " " + Character.toString(l));
        System.out.print(Character.toString(l));
    }
}



