package Star3D;

import javax.swing.JFrame;

/**
 *
 * @author Ceud
 */
public class test extends JFrame {


    public test() {

        setTitle("Star3D Viewer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);

        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);

        StarField field = new StarField(this);
        add(field);

        field.initialise();

        //fix for canvas not rendering on some instances
        this.setState(JFrame.ICONIFIED);
        this.setState(JFrame.NORMAL);

    }


    public static void main(String[] args) {
        new test();
    }

}
