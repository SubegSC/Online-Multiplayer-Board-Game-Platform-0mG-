import javax.swing.*;
import java.awt.*;

public class MainGui {
  //went with a very dark gray for the background cuz I like dark modes
  //went with an off-white for text cuz it's less harsh against black

  private static final Color bgCol = new Color(20, 20, 20);
  private static final Color tCol = new Color(228, 228, 228);
  private static final Color btnCol = new Color(35,35,35);
  public static void main(String[] args) {
    SwingUtilities.invokeLater(MainGui::start);
  }

  static void start() {
    JFrame frame = new JFrame("OMG -- Online Multiplayer Board Game Platform");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);

    JPanel base = new JPanel(new BorderLayout(16,16)); //let's use border layout to arrange things
    base.setBackground(bgCol);
    frame.setContentPane(base);
    frame.setVisible(true);

    JLabel title = new JLabel("Online Multiplayer Board Game Platform", SwingConstants.CENTER);
    title.setForeground(tCol);
    title.setFont(title.getFont().deriveFont(24f));
    title.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    base.add(title, BorderLayout.NORTH); //add our title to the base

    //at this stage, let's draft up three buttons to match our use cases/descriptions
    JButton loginBtn = new JButton("Log In");
    JButton regBtn = new JButton("Register");
    JButton lbrdBtn = new JButton("View Leaderboard");

    JButton[] buttons = {loginBtn, regBtn, lbrdBtn};
    for (JButton button : buttons){
        button.setBackground(btnCol);
        button.setForeground(tCol);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    JPanel startButtons = new JPanel();
    startButtons.setOpaque(false);
    startButtons.setLayout(new BoxLayout(startButtons, BoxLayout.Y_AXIS));

    startButtons.add(Box.createVerticalGlue());
    startButtons.add(loginBtn);
    startButtons.add(Box.createVerticalStrut(10));
    startButtons.add(regBtn);
    startButtons.add(Box.createVerticalStrut(10));
    startButtons.add(lbrdBtn);
    startButtons.add(Box.createVerticalGlue());

    base.add(startButtons, BorderLayout.CENTER);

    //maybe we can use mouse listeners to change the box color when you hover
  }
}
