import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ScreenCap extends JFrame implements KeyListener {
    private Robot bot; //robot used to capture screenshot
    private JPanel panel; //gridlayout panel that is placed on this (jframe)
    private JFileChooser jfc; //file chooser to choose where to capture photos and rename files
    private JTextField name, number; //text fields for name and number input
    private JScrollPane scrollpane; //scrollpane that textarea is put into so it gets a scrollbar
    private JTextArea area; //area where all feed of actions are displayed and updated in

    private int fileNum; //picture counter, updates after every picture is taken or file is renamed
    private File directory;
    private File[] fileList;
    private String fileName; //currnet name chosen for pictures captured or files renamed
    private String feed; //text area feed that is updated after every action
    private String dirName; //directory name variable used whenever user decides to chance directories
    private String appName = "ScreenCap"; //app name variable in case i change the app name and dont have to replace it everywhere
    private String help;

    private boolean fileNameFlag = false;
    private boolean fileExtensionFlag = false;

    private double finalSize = 0;

    private String formats[] = new String[]{".jpeg", ".jpg", ".png", ".jfif", ".gif", ".webm", ".mp4", ".mkv", ".mov"};

    public ScreenCap() throws AWTException {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) { 
            System.err.println("Error: " + e.getMessage()); 
        }

        jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setFileHidingEnabled(true);
        jfc.showOpenDialog(null);
        directory = jfc.getSelectedFile();
        fileList = directory.listFiles();
        Arrays.sort(fileList, Comparator.comparingLong(File::lastModified));
        dirName = directory.toString() + "\\";
        this.setTitle(appName + " | " + dirName);

        //Setup
        bot = new Robot();
        fileName = "";
        fileNum = -1;

        //JPanel 
        panel = new JPanel(new BorderLayout());

        //Font
        Font font = new Font("Dialog", Font.PLAIN, 15);

        //JTextField
        JPanel fieldpanel = new JPanel(new GridLayout(1, 2));
        name = new JTextField();
        name.setFont(font);
        name.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fileName = name.getText();

                    String newFeed = "Name has been changed to \"" + fileName + "\"";
                    setFeed(newFeed);
                }
            } 
        });

        number = new JTextField();
        number.setFont(font);
        number.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        fileNum = Integer.parseInt(number.getText());
                        String newFeed = "Number has been changed to \"" + fileNum + "\"";
                        setFeed(newFeed);
                    } catch (NumberFormatException f) {
                        setFeed("Please enter a real number, \"" + number.getText() + "\" includes non-integer characters");
                    }
                }
            }

        });
    
        fieldpanel.add(name);
        fieldpanel.add(number);
        
        //JTextArea
        JPanel textpanel = new JPanel(new BorderLayout());
        area = new JTextArea("");
        area.setEditable(false);
        area.addKeyListener(this);
        area.setFont(font);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        feed = "Welcome to " + appName + "!";
        //help = "\nType desired name and number in the two text boxes above and set them with \"Enter\"";
        help = "\nType \"Enter\" to set desired name and number in the two text boxes above";
        help += "\nIf you're trying to convert files, input desired file format in the name text box";
        help += "\nType \"C\" to take a screnshot of your main monitor";
        help += "\nType \"R\" to rename all files starting from your currently set name and number";
        help += "\nType \"E\" to convert all files based on the set file format, include the period";
        help += "\nType \"F\" to choose a different file directory";
        help += "\nType \"L\" to know the name of the latest file in the directory";
        help += "\nType \"H\" to reprint these directions";
        help += "\nAll key commands must be done while the bottom text area is selected";
        help += "\nFile formats \"" + printFormats(formats) + "\" are supported";
        help += "\n";
        feed += help;

        area.setText(feed);
        textpanel.add(area);

        //JScrollPane
        scrollpane = new JScrollPane(textpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); //scrollpane basically becomes textpanel, just has a scrollbar
        scrollpane.getVerticalScrollBar().setUnitIncrement(16);

        //Adding to JPanel
        panel.add(fieldpanel, BorderLayout.NORTH);
        panel.add(scrollpane, BorderLayout.CENTER);

        //JFrame Stuff
        this.add(panel); 
        this.addKeyListener(this);
        this.setFocusable(true);
        this.setSize(600, 550);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("src/minjeong.png")).getImage());
        this.setVisible(true);
    }

    public String printFormats(String[] arr) {
        String str = "";

        for (String s : arr) {
            str+= s.substring(1) + ", "; //substring 1 to get rid of the . when printing help
        }

        return str.substring(0, str.length()-2); //-2 to get rid of the , and space after the last item
    }


    public void setFeed(String newFeed) {
        feed += "\n" + newFeed;
        area.setText(feed);
    }

    public void capture(Robot bot) throws IOException {
        //Detects correct numbers, captures lots of black space
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        //Captures halved numbers, correctly captures main screen at halved resolution
        // Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        // int width = (int) screen.getWidth();
        // int height = (int) screen.getHeight();

        System.out.println(width + "x" + height);
        Rectangle screenSize = new Rectangle(width, height);

        BufferedImage img = bot.createScreenCapture(screenSize);
        File output = new File(dirName + fileName + " " + fileNum + ".png");
        ImageIO.write(img, "png", output);
        // System.out.println(width + " " + height);
    }


    public void renameAll() {
        boolean error = false;
        boolean pass = false;
        int count = 0;

        for (File file : fileList) {
            String currentFile = file.toString();

            for (String format : formats) { //note: before this used to ignore .ini files (custom folder icons)
                if (currentFile.indexOf(format) != -1) { //enables only allowed formats to be editted to avoid mass destroying my pc
                    pass = true;
                    String rename = dirName + fileName + " " + fileNum + currentFile.substring(currentFile.lastIndexOf("."));
                    file.renameTo(new File(rename));

                    String newFeed = "\"" + currentFile.substring(currentFile.lastIndexOf("\\")+1) + "\" has been renamed to \"" + rename.substring(rename.lastIndexOf(fileName)) + "\"";
                    setFeed(newFeed);

                    fileNum++;
                    number.setText(fileNum + "");
                }
            }

            if (!pass) {
                error = true;
                count++;
            }

            pass = false;
        }

        if (error) {
            if (count > 1) {
                setFeed(count + " unsupported file formats found and thus ignored, press \"H\" for more info");
            } else {
                setFeed(count + " unsupported file format found and thus ignored, press \"H\" for more info");
            }
        }
    }

    public void convertFiles() {
        boolean error = false;

        for (File file : fileList) {
            String str = file.toString();

            for (String format : formats) { //note: before this used to ignore .ini files (custom folder icons)
                if (str.indexOf(format) != -1) { //enables only allowed formats to be editted to avoid mass destroying my pc
                    String rename = str.substring(0, str.lastIndexOf(".")) + fileName;
                    file.renameTo(new File(rename));
                    //"C:\Users\hydro\OneDrive\Desktop\ScreepCap Test 2\Aespa_Winter_Girls_concept_photo_33.webp"
                    String newFeed = "\"" + str.substring(str.lastIndexOf("\\")+1) + "\" has been converted to \"" + rename.substring(rename.lastIndexOf("\\")+1) + "\"";
                    setFeed(newFeed);
                } else {
                    error = true;
                }
            }
            
        }

        if (error) {
            setFeed("Unsupported file formats found and ignored");
        }
    }

    public void readFiles(File[] folder) {
        double count = 0;
        double kb = 1024;
        double mb = kb*kb;
        double gb = mb*kb;
        int roundBy = 10;

        //System.out.println("Actual: " + folder);


        for (File file : folder) {
            if (file.isDirectory()) {
                try {
                    readFiles(file.listFiles());
                } catch (Exception e) {
                    //System.out.println("fart");
                }
            } else {
                //Commented this guy out because he was yellow and i didnt want that
                // String str = file.toString(); 
                double fileSize = file.length();
                count += file.length();

                if (fileSize >= gb) {
                    fileSize /= gb;
                    fileSize = Math.round(fileSize * roundBy);
                    fileSize = fileSize/roundBy;
                    //System.out.println(str.substring(str.lastIndexOf("\\")+1) + " - " + fileSize + " gigabytes");


                } else if (fileSize >= mb) {
                    fileSize /= mb;
                    fileSize = Math.round(fileSize * roundBy);
                    fileSize = fileSize/roundBy;
                    //System.out.println(str.substring(str.lastIndexOf("\\")+1) + " - " + fileSize + " megabytes");

                } else {
                    fileSize /= kb;
                    fileSize = Math.round(fileSize * roundBy);
                    fileSize = fileSize/roundBy;
                    //System.out.println(str.substring(str.lastIndexOf("\\")+1) + " - " + fileSize + " kilobytes");
                }
            }
        }

        finalSize += count;
    }

    public void countSize() {
        double kb = 1024;
        double mb = kb*kb;
        double gb = mb*kb;
        int roundBy = 10;

        readFiles(fileList);

        if (finalSize >= gb) {
            finalSize /= gb;
            finalSize = Math.round(finalSize * roundBy);
            finalSize = finalSize/roundBy;
            System.out.println("TOTAL: " + finalSize + " gigabytes");

        } else if (finalSize >= mb) {
            finalSize /= mb;
            finalSize = Math.round(finalSize * roundBy);
            finalSize = finalSize/roundBy;
            System.out.println("TOTAL: " + finalSize + " megabytes");

        } else {
            finalSize /= kb;
            finalSize = Math.round(finalSize * roundBy);
            finalSize = finalSize/roundBy;
            System.out.println("TOTAL: " + finalSize + " kilobytes");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        //Screenshot
        if (keyCode == KeyEvent.VK_C) {
            if (fileName != "" && fileNum != -1) {
                try {
                    capture(bot);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                String newFeed = "Saved " + fileName + " " + fileNum + ".png";
                setFeed(newFeed);

                fileNum++;
                number.setText(fileNum+"");
            } else {
                setFeed("Please set a name or number first");
            }            
        }
        
        
        //Rename all files
        if (keyCode == KeyEvent.VK_R) { 
            setFeed("Are you sure you want to rename all files in \"" + dirName + "\" to \"" + fileName + " " + fileNum + "\" and onwards? Type \"Y\" or \"N\" to confirm");
            fileNameFlag = true;
        }

        if (fileNameFlag) {
            if (keyCode == KeyEvent.VK_Y) {
                renameAll();
                fileNameFlag = false;
            }

            if (keyCode == KeyEvent.VK_N) {
                setFeed("Okay, your files will not be renamed");
                fileNameFlag = false;
            }
        }


        //Rename file extensions of all files, file conversion (kindve?)
        if (keyCode == KeyEvent.VK_E) {
            if (fileName.indexOf(".") != -1) { //is input name/file extension really a file extension? Right now its period but later i shall add array or text file full of file formats
                setFeed("Are you sure you want to convert all files in \"" + dirName + "\" to the \"" + fileName + "\" file format? Make sure that the files you are trying to convert support this file format. Type \"Y\" or \"N\" to confirm");
                fileExtensionFlag = true;

            } else {
                setFeed("Try again, file extension name (including the period) must be entered in name text field");
            }
        }

        if (fileExtensionFlag) {
            if (keyCode == KeyEvent.VK_Y) {
                convertFiles();
                fileExtensionFlag = false;
            }

            if (keyCode == KeyEvent.VK_N) {
                setFeed("Okay, your files will not be converted");
                fileExtensionFlag = false;
            }
        }


        //Change directory
        if (keyCode == KeyEvent.VK_F) {
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setFileHidingEnabled(true);
            jfc.showOpenDialog(null);
            directory = jfc.getSelectedFile();
            fileList = directory.listFiles();
            Arrays.sort(fileList, Comparator.comparingLong(File::lastModified));
            dirName = directory.toString() + "\\";
            this.setTitle(appName + " | " + dirName);

            setFeed("Directory has been changed to \"" + dirName + "\"");
        }

        //List last file
        if (keyCode == KeyEvent.VK_L) {
            try {
                String str = fileList[fileList.length-1].toString();
                String newFeed = str.substring(str.lastIndexOf("\\")+1);

                setFeed("\"" + newFeed + "\" is the last file in the chosen directory");

            } catch (ArrayIndexOutOfBoundsException a) {
                setFeed("The current directory is empty, please try again");
            }
            
        } 
        
        //Removed until i fix the whole null file/folder problem, not really sure what it is rn and not really sure if i want to keep this part of the app, if i am keeping this part of the app i do need to list out from greatest to least the sizes of each directory meaning id have to like store the directory, or just setFeed when i get to it, but i'd only see it in the feed once the program finished since if its reading a whole biggo thing it takes A WHILE
        // if (keyCode == KeyEvent.VK_S) {
        //     countSize();
        // }

        //Reprints the help string/guide
        if (keyCode == KeyEvent.VK_H) {
            setFeed(help);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}


    public static void main(String[] args) throws AWTException {
        new ScreenCap();
    }
}
