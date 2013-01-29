package Star3D;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.swing.JFrame;

/**
 *
 * @author Ceud
 */
public class StarField extends Canvas implements MouseListener, MouseMotionListener {

    JFrame frame;
    int[] origin = new int[3];
    int[] target = new int[3];
    String msg = "";

    Star3D[] stars = new Star3D[50];
    Star3D[] offsetStars = new Star3D[50];
    Star2D[] tmpPoints;
    int zDist[] = new int[50];

    int width, height;
    int mx, my;  // the most recently recorded mouse coordinates

    Image backbuffer;
    Graphics backg;

    int azimuth = 0, elevation = 0;
    private boolean isMoving = false;

    public StarField(JFrame FRAME) {
        origin[0] = 0;
        origin[1] = 0;
        origin[2] = 0;

        target[0] = 0;
        target[1] = 0;
        target[2] = 0;

        frame = FRAME;
        width = frame.getWidth();
        height = frame.getHeight();
        loadStars();
        offsetStars = this.calcOffsetCoords();  //  sol: 0,0,0
    }

    /**
     * Calculates the offset coordinates from origin for Star3D objects
     *
     * @param offset
     * @return Star3D[] array of Star3D objects with origin offset coordinates
     */
    private Star3D[] calcOffsetCoords() {
        Star3D[] starList = new Star3D[stars.length];
        for (int j = 0; j < stars.length; j++) {
            float fX = stars[j].x - origin[0]; //offset.x;
            float fY = stars[j].y - origin[1]; //offset.y;
            float fZ = stars[j].z - origin[2]; //offset.z;
            starList[j] = new Star3D(fX, fY, fZ, stars[j].name, stars[j].classification);
        }
        return starList;
    }

    public int atStar(float X, float Y, float Z) {
        int star = -1;
        for (int j = 0; j < stars.length; j++) {
            if (stars[j].x == X && stars[j].y == Y && stars[j].z == Z) {
                star = j;
                break;
            }
        }
        return star;
    }

    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }

    public void mouseClicked( MouseEvent e ) {
        int tmpStar = this.getStarIndexAtCoords(tmpPoints);
        if (tmpStar >= 0) {
            origin[0] = Math.round(stars[tmpStar].x);
            origin[1] = Math.round(stars[tmpStar].y);
            origin[2] = Math.round(stars[tmpStar].z);


            offsetStars = this.calcOffsetCoords();
        // update the backbuffer
        drawWireframe( backg );

        repaint();
        e.consume();
        }
    }
    public void mousePressed( MouseEvent e ) {
        mx = e.getX();
        my = e.getY();
    }
    public void mouseReleased( MouseEvent e ) { }

    public void mouseMoved( MouseEvent e ) {
        int tmpStar = this.getStarIndexAtCoords(tmpPoints);
        if (tmpStar >= 0) {
            msg = stars[tmpStar].name + " [" + stars[tmpStar].x + ", " + stars[tmpStar].y + ", " + stars[tmpStar].z + "]";
        }
        else {
            msg = "";
        }
        // update the backbuffer
        drawWireframe( backg );

        repaint();
        e.consume();

    }

    public void mouseDragged( MouseEvent e ) {

        // get the latest mouse position
        int new_mx = e.getX();
        int new_my = e.getY();

        // adjust angles according to the distance travelled by the mouse
        // since the last event
        azimuth -= new_mx - mx;
        elevation += new_my - my;

        normaliseAzimuth(azimuth);
        normaliseElevation(elevation);

        // update the backbuffer
        drawWireframe( backg );

        // update our data
        mx = new_mx;
        my = new_my;

        repaint();
        e.consume();
    }



    public void initialise() {
        backbuffer = createImage( width, height );
        backg = backbuffer.getGraphics();

        drawWireframe( backg );

        repaint();

        addMouseListener( this );
        addMouseMotionListener( this );


    }

    @Override
    public void update( Graphics g ) {
        g.drawImage( backbuffer, 0, 0, this );
    }

    @Override
    public void paint( Graphics g ) {
        update( g );
    }

    public Star2D getStarAtCoords(Star2D[] points) {
        //Star2D star = null;
        if (this.getMousePosition() != null) {
            for (int i = 0; i < points.length; i++) {

            //250 = cutoff for sizing, all beyond this are DIAMETER...
            float ddd = (float) (250 / zDist[i]);
            if (ddd < 1) { ddd = 1f; }
            int tmpOff = (int) (Star2D.DIAMETER * ddd / 2);

                if (points[i].intersects(this.getMousePosition().x, this.getMousePosition().y, tmpOff)) {
                    return points[i];
                }
            }
        }
        return null;
    }

    public int getStarIndexAtCoords(Star2D[] points) {
        if (this.getMousePosition() != null) {
            for (int i = 0; i < points.length; i++) {
                if (zDist[i] > 0) {
            //250 = cutoff for sizing, all beyond this are DIAMETER...
            float ddd = (float) (250 / zDist[i]);
            if (ddd < 1) { ddd = 1f; }
            int tmpOff = (int) Math.round(Math.abs(Star2D.DIAMETER * ddd / 2));

                if (points[i] != null && points[i].intersects(this.getMousePosition().x, this.getMousePosition().y, tmpOff)) {
                    return i;
                }
            }
                }
        }
        return -1;
    }

    private void normaliseAzimuth(int azimuth) {
        while (azimuth > 180) {
            azimuth -= 360;
        }
        while (azimuth <= -180) {
            azimuth += 360;
        }
        this.azimuth = azimuth;
    }

    private void normaliseElevation(int elevation) {
        while (elevation > 180) {
            elevation -= 360;
        }
        while (elevation <= -180) {
            elevation += 360;
        }
        this.elevation = elevation;
    }

   private void drawWireframe( Graphics g ) {

       Graphics2D g2d = (Graphics2D) g;

      // compute coefficients for the projection
      double theta = Math.PI * azimuth / 180.0;
      double phi = Math.PI * elevation / 180.0;
      float cosT = (float)Math.cos( theta ), sinT = (float)Math.sin( theta );
      float cosP = (float)Math.cos( phi ), sinP = (float)Math.sin( phi );
      float cosTcosP = cosT*cosP, cosTsinP = cosT*sinP,
            sinTcosP = sinT*cosP, sinTsinP = sinT*sinP;

      // project vertices onto the 2D viewport
      Star2D[] points;
      points = new Star2D[ offsetStars.length];  //change to only use ones in view


      int j;
      int scaleFactor = width/4;
      float near = 3f;  // distance from eye to near plane
      float nearToObj = 1.5f;  // distance from near plane to center of object
      for ( j = 0; j < offsetStars.length; j++ ) {
         int x0 = (int) offsetStars[j].x;
         int y0 = (int) offsetStars[j].y;
         int z0 = (int) offsetStars[j].z;

         // compute an orthographic projection
         float x1 = cosT*x0 + sinT*z0;
         float y1 = -sinTsinP*x0 + cosP*y0 + cosTsinP*z0;

         // now adjust things to get a perspective projection
         float z1 = cosTcosP*z0 - sinTcosP*x0 - sinP*y0;
         x1 = x1*near/(z1+near+nearToObj);
         y1 = y1*near/(z1+near+nearToObj);

            int tmpX = (int)(width/2 + scaleFactor*x1 + 0.5);
            int tmpY = (int)(height/2 - scaleFactor*y1 + 0.5);

            zDist[j] = Math.round(Math.abs(z1));

            if (tmpX >= 0 - Star2D.displayOffset()
                    && tmpX < width + Star2D.displayOffset()
                    && tmpY >= 0 - Star2D.displayOffset()
                    && tmpY < height + Star2D.displayOffset()
                    && z1 < 0) {
         // the 0.5 is to round off when converting to int
         points[j] = new Star2D(
            (int)(width/2 + scaleFactor*x1 + 0.5),
            (int)(height/2 - scaleFactor*y1 + 0.5),
            offsetStars[j].name,
            offsetStars[j].classification
         );
            }
            else {
                //not in viewable area so set to null
                points[j] = null;
            }
      }
      tmpPoints = points;

      // draw the stars
      g2d.setColor( Color.black );
      g2d.fillRect( 0, 0, width, height );
      g2d.setColor( Color.white );

      Font fnt = new Font("Fixedsys", Font.PLAIN, 10);
      g2d.setFont(fnt);

      RadialGradientPaint rg;
      for ( j = 0; j < points.length; j++ ) {
          if (j != atStar(origin[0], origin[1], origin[2]) && points[j] != null) {

         Color[] cols = {points[j].getColour(), Color.black};
         float[] frac = {0.5f, 1.0f};

         //250 = cutoff for sizing, all beyond this are DIAMETER...
         float ddd = (float) (250 / zDist[j]);
         if (ddd < 1) { ddd = 1f; }
         int tmpOff = (int) (Star2D.DIAMETER * ddd / 2);

         g2d.setColor( Color.white );
         g2d.drawString(points[j].name, (float) points[j].x - (2 * points[j].name.length()), (float) points[j].y - (5 + (tmpOff)));
         g2d.setColor( points[j].getColour() );


         rg = new RadialGradientPaint(points[j].x, points[j].y, (float) (Star3D.getRadius() * ddd), frac, cols);
         g2d.setPaint(rg);

         g2d.fillOval((int) (points[j].x - tmpOff),
                 (int) (points[j].y - tmpOff),
                 (int) (Star2D.DIAMETER * ddd),
                 (int) (Star2D.DIAMETER * ddd));
          }
      }
      g2d.setColor( Color.white );

      fnt = new Font("Fixedsys", Font.BOLD, 12);
      g2d.setFont(fnt);
      if (atStar(origin[0], origin[1], origin[2]) >= 0) {
        g2d.drawString("Viewing from " + stars[atStar(origin[0], origin[1], origin[2])].name + " [" + stars[atStar(origin[0], origin[1], origin[2])].x + ", " + stars[atStar(origin[0], origin[1], origin[2])].y + ", " + stars[atStar(origin[0], origin[1], origin[2])].z + "]", 10, height - 50);
      }
      else {
        g2d.drawString("Viewing from space [" + origin[0] + "," + origin[1] + "," + origin[2] + "]", 10, height - 50);
      }
      g2d.drawString("Azimuth: " + azimuth + "°, Elevation: " + elevation + "°", 10, height - 30);

      if (msg.length() > 0) {
          g2d.drawString(msg, 10, 25);
      }

    }

    private void loadStars() {
        try {
            FileInputStream fis = new FileInputStream("stars.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);

            Star3DStore star;
            Object obj;
            int i = 0;
            while ((obj = ois.readObject()) != null) {
                if (obj instanceof Star3DStore) {
                    star = (Star3DStore) obj;
                    stars[i] = new Star3D(star.x, star.y, star.z, star.name, star.classification);
                    i++;
                }
            }

            ois.close();
        } catch (EOFException e) {
            System.out.println("EOF reached");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadStars2() {

        stars[0] = new Star3D(0,0,0,"Sol","G");
        stars[1] = new Star3D(-17,-13,-38,"Alpha Centauri","G");
        stars[2] = new Star3D(-1,-59,5,"Barnard`s Star","M");
        stars[3] = new Star3D(-74,21,9,"Wolf 359","M");
        stars[4] = new Star3D(-65,17,49,"Lalande 21185","M");
        stars[5] = new Star3D(-16,81,-23,"Sirius","A");
        stars[6] = new Star3D(75,35,27,"Luyten 726-8","M");
        stars[7] = new Star3D(19,-86,39,"Ross 154","M");
        stars[8] = new Star3D(74,-6,72,"Ross 248","M");
        stars[9] = new Star3D(62,83,-16,"Epsilon Eridani","K");
        stars[10] = new Star3D(86,-21,-60,"Lacaille 9352","M");
        stars[11] = new Star3D(-109,6,2,"Ross 128","M");
        stars[12] = new Star3D(102,-38,-29,"EZ Aquarii","M");
        stars[13] = new Star3D(65,-60,72,"61 Cygni","K");
        stars[14] = new Star3D(-48,103,10,"Procyon","F");
        stars[15] = new Star3D(11,-57,99,"Struve 2398","M");
        stars[16] = new Star3D(83,7,81,"Groombridge 34","M");
        stars[17] = new Star3D(59,-33,-97,"Epsilon Indi","K");
        stars[18] = new Star3D(-64,84,53,"DX Cancri","M");
        stars[19] = new Star3D(104,51,-29,"Tau Ceti","G");
        stars[20] = new Star3D(51,70,-83,"GJ 1061","M");
        stars[21] = new Star3D(111,36,-31,"YZ Ceti","M");
        stars[22] = new Star3D(-46,114,11,"Luyten`s Star","M");
        stars[23] = new Star3D(87,82,36,"Teegarden`s Star","M");
        stars[24] = new Star3D(12,-58,-111,"SCR 1845-6357","M");
        stars[25] = new Star3D(19,88,-90,"Kapteyn`s Star","M");
        stars[26] = new Star3D(78,-67,-78,"Lacaille 8760","M");
        stars[27] = new Star3D(65,-27,111,"Kruger 60","M");
        stars[28] = new Star3D(-99,32,-81,"DEN 1048-3956","M");
        stars[29] = new Star3D(-17,132,-3,"Ross 614","M");
        stars[30] = new Star3D(-52,-125,-27,"Gl 628","M");
        stars[31] = new Star3D(137,30,13,"Van Maanen`s Star","D");
        stars[32] = new Star3D(114,3,-85,"Gl 1","M");
        stars[33] = new Star3D(-16,-140,22,"Wolf 424","M");
        stars[34] = new Star3D(122,71,33,"TZ Arietis","M");
        stars[35] = new Star3D(-6,-54,137,"Gl 687","M");
        stars[36] = new Star3D(51,-94,-102,"GJ 1245","M");
        stars[37] = new Star3D(-14,-104,-105,"Gl 674","M");
        stars[38] = new Star3D(-14,-104,-105,"LHS 292","M");
        stars[39] = new Star3D(-68,4,-134,"GJ 440","D");
        stars[40] = new Star3D(152,4,-17,"GJ 1002","M");
        stars[41] = new Star3D(143,-43,-36,"Ross 780","M");
        stars[42] = new Star3D(-72,25,-136,"LHS 288","M");
        stars[43] = new Star3D(-112,27,109,"GJ 412","M");
        stars[44] = new Star3D(-92,47,120,"GJ 380","K");
        stars[45] = new Star3D(-136,64,54,"GJ 388","M");
        stars[46] = new Star3D(85,-63,-121,"GJ 832","M");
        stars[47] = new Star3D(77,109,-92,"LP 944-020","M");
        stars[48] = new Star3D(80,76,-118,"DEN 0255-4700","L");
        stars[49] = new Star3D(-12,-118,-113,"GJ 682","M");
    }
}
