package org.opencraft.nbtedit;

import com.trolltech.qt.gui.*;

public class Main {
    public static void main(String args[]) {
        QApplication.initialize(args);

        QPushButton quit = new QPushButton("Quit");
        quit.resize(80, 40);
        quit.setFont(new QFont("Times", 18, QFont.Weight.Bold.value()));

        quit.clicked.connect(QApplication.instance(), "quit()");

        quit.setWindowTitle("Quit example");
        quit.show();
        QApplication.exec();
    }
} 
