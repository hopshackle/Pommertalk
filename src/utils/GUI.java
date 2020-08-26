package utils;

import core.Game;
import players.HumanPlayer;
import players.KeyController;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static utils.Types.*;

public class GUI extends JFrame {
    private JLabel appTick;
    private GameView[] views;
    private Game game;
    private AvatarView avatarDisplayPanel;
    private KeyController ki;
    private int humanIdx;  // human player index in array of players
    private boolean displayPOHuman;  // if side views should be displayed when human is playing

    private int playerNo;

    /**
     * Constructor
     * @param title Title of the window.
     */
    public GUI(Game game, String title, KeyController ki, boolean closeAppOnClosingWindow, boolean displayPOHuman) {
        super(title);
        this.game = game;
        this.ki = ki;
        this.displayPOHuman = displayPOHuman;

        // Check if a human is playing
        checkHumanPlaying();

        // Create all the game views. Main view is first in list, showing true fully observable game state
        createGameViews();

        // Create frame layout
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 0;
        setLayout(gbl);

        // Main panel definition
        JPanel mainPanel = getMainPanel();

        // Add everything to side panel if we need it to be displayed
        JPanel poPanel = getPoPanel();

        /* Add all elements to the content pane */

        // Content + side padding
        gbc.gridx = 0;
        getContentPane().add(Box.createRigidArea(new Dimension(10, 0)), gbc);
        gbc.gridx++;
        getContentPane().add(mainPanel, gbc);
        gbc.gridx++;
        getContentPane().add(Box.createRigidArea(new Dimension(10, 0)), gbc);
        if (poPanel != null) {
            gbc.gridx++;
            getContentPane().add(poPanel, gbc);
        }
        gbc.gridx++;
        getContentPane().add(Box.createRigidArea(new Dimension(10, 0)), gbc);

        // Bottom row, bottom margin padding
        gbc.gridy++;
        gbc.gridx = 0;
        getContentPane().add(Box.createRigidArea(new Dimension(0, 5)), gbc);

        // Frame properties
        pack();
        this.setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        if(closeAppOnClosingWindow){
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
        repaint();
    }

    /**
     * Checks if one of the players in the game is human.
     */
    private void checkHumanPlaying() {
        humanIdx = -1;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i) instanceof HumanPlayer) {
                humanIdx = i;
                break;
            }
        }
    }

    /**
     * Creates game views to be displayed.
     */
    private void createGameViews() {
        views = new GameView[game.nPlayers() + 1];  // N individual views + main true game state
        for (int i = 0; i < views.length; i++) {
            int pIdx = i - 1;
            int cellSize = CELL_SIZE_PO;

            if (i == 0) {
                cellSize = CELL_SIZE_MAIN;  // Main view will have a different cell size to the rest
                if (humanIdx > -1)
                    // If human is playing, main view will be human view, and not true game state
                    pIdx = humanIdx;
            }
            else if (humanIdx > -1 && !displayPOHuman)
                // If a human is playing and we don't need to display the other PO views, leave them null
                break;

            views[i] = new GameView(game.getBoard(pIdx), cellSize);
        }
    }

    /**
     * Creates the main panel containing main view of true game state (or human if human playing).
     * Includes information about the game:
     *  - game tick
     *  - game mode
     *  - game avatars status
     * @return main panel.
     */
    @SuppressWarnings("Duplicates")
    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTH;
        c.weighty = 0;

        JLabel appTitle = new JLabel("Java-Pommerman");
        Font textFont = new Font(appTitle.getFont().getName(), Font.BOLD, 20);
        appTitle.setFont(textFont);

        JLabel modeLabel = new JLabel("game mode: " + game.getGameMode());
        textFont = new Font(appTitle.getFont().getName(), Font.PLAIN, 16);
        modeLabel.setFont(textFont);

        appTick = new JLabel("tick: 0");
        appTick.setFont(textFont);

        avatarDisplayPanel = new AvatarView(game.getAvatars(-1));

        JLabel allianceLabel = new JLabel("current alliances");
        textFont = new Font(appTitle.getFont().getName(), Font.PLAIN, 16);
        allianceLabel.setFont(textFont);

        // Add everything to main panel
        mainPanel.add(appTitle, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)), c);
        c.gridy++;
        mainPanel.add(appTick, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)), c);
        c.gridy++;
        mainPanel.add(modeLabel, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)), c);
        c.gridy++;
        mainPanel.add(avatarDisplayPanel, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)), c);
        c.gridy++;
        mainPanel.add(views[0], c);

        c.gridy++;
        // test alliances
        boolean[][] testAlliance = {{false, false, true, false}, {false, true, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, true}};
        JPanel alliancePanel = getAlliancePanel(playerNo, testAlliance);
        /**boolean[][] testAlliance2 = {{false, false, true, false}, {false, true, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, true}};
        JPanel alliancePanel2 = getAlliancePanel(playerNo, testAlliance2);
        boolean[][] testAlliance3 = {{false, false, true, false}, {false, true, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, true}};
        JPanel alliancePanel3 = getAlliancePanel(playerNo, testAlliance3);
        boolean[][] testAlliance4 = {{false, false, true, false}, {false, true, false, false}, {false, false, false, false}, {false, false, false, false}, {false, false, false, true}};
        JPanel alliancePanel4 = getAlliancePanel(playerNo, testAlliance4);
        JPanel[] allAlliances = {alliancePanel1, alliancePanel2, alliancePanel3, alliancePanel4};**/

        // Whilst PLAYING
        // Must disable buttons for human player controls to work
        /**Component[] components = alliancePanel.getComponents();
        for(Component component : components)
            component.setEnabled(false);**/
        
        mainPanel.add(alliancePanel, c);

        return mainPanel;
    }

    /**
     * Creates the side panel.
     * @return null if side panel should not be displayed, side panel otherwise.
     */
    private JPanel getPoPanel() {
        JPanel poPanel = null;
        if (humanIdx == -1 || displayPOHuman) {
            poPanel = new JPanel();
            poPanel.setLayout(new BoxLayout(poPanel, BoxLayout.Y_AXIS));
            poPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            for (int i = 1; i < views.length; i++) {
                poPanel.add(views[i]);
                poPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        return poPanel;
    }

    /**
     * Creates Alliance Panel
     */
    private JPanel getAlliancePanel(int player, boolean[][] alliances) {

        JPanel alliancePanel = new JPanel();

        if(player == -1)
            return alliancePanel;

        alliancePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel allianceLabel = new JLabel("current alliances: player " + (player+1));
        Font textFont = new Font(allianceLabel.getFont().getName(), Font.PLAIN, 16);
        allianceLabel.setFont(textFont);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        alliancePanel.add(allianceLabel, c);

        JToggleButton rule1 = new JToggleButton("Rule 1");
        //rule1.setEnabled(false);
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 75;
        alliancePanel.add(rule1, c);

        JToggleButton rule2 = new JToggleButton("Rule 2");
        rule2.setEnabled(false);
        c.gridy = 2;
        alliancePanel.add(rule2, c);

        JToggleButton rule3 = new JToggleButton("Rule 3");
        rule3.setEnabled(false);
        c.gridy = 3;
        alliancePanel.add(rule3, c);

        JToggleButton rule4 = new JToggleButton("Rule 4");
        rule4.setEnabled(false);
        c.gridy = 4;
        alliancePanel.add(rule4, c);

        JToggleButton rule5 = new JToggleButton("Rule 5");
        rule5.setEnabled(false);
        c.gridy = 5;
        alliancePanel.add(rule5, c);

        // Button icons
        Icon agent0lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0lo.png"));
        Icon agent0 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0.png"));
        Icon agent1lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1lo.png"));
        Icon agent1 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1.png"));
        Icon agent2lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2lo.png"));
        Icon agent2 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2.png"));
        Icon agent3lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3lo.png"));
        Icon agent3 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3.png"));

        JToggleButton[][] allianceArray = new JToggleButton[5][4];

        for(int i = 0; i < allianceArray.length; i++)
        {
            for(int j = 0; j < allianceArray[0].length; j++)
            {
                switch(j)
                {
                    case 0:
                        allianceArray[i][j] = new JToggleButton("", agent0lo);
                        allianceArray[i][j].setSelectedIcon(agent0);
                        allianceArray[i][j].setDisabledSelectedIcon(agent0);
                        allianceArray[i][j].setDisabledIcon(agent0lo);
                        allianceArray[i][j].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 1:
                        allianceArray[i][j] = new JToggleButton("", agent1lo);
                        allianceArray[i][j].setSelectedIcon(agent1);
                        allianceArray[i][j].setDisabledSelectedIcon(agent1);
                        allianceArray[i][j].setDisabledIcon(agent1lo);
                        allianceArray[i][j].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 2:
                        allianceArray[i][j] = new JToggleButton("", agent2lo);
                        allianceArray[i][j].setSelectedIcon(agent2);
                        allianceArray[i][j].setDisabledSelectedIcon(agent2);
                        allianceArray[i][j].setDisabledIcon(agent2lo);
                        allianceArray[i][j].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 3:
                        allianceArray[i][j] = new JToggleButton("", agent3lo);
                        allianceArray[i][j].setSelectedIcon(agent3);
                        allianceArray[i][j].setDisabledSelectedIcon(agent3);
                        allianceArray[i][j].setDisabledIcon(agent3lo);
                        allianceArray[i][j].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                }
            }
        }

            c.ipadx = 0;
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            switch(player)
            {
                case 0:
                    alliancePanel.add(allianceArray[0][1], c);
                    allianceArray[0][1].setSelected(alliances[0][1]);
                    break;
                case 1: case 2: case 3:
                    alliancePanel.add(allianceArray[0][0], c);
                    allianceArray[0][0].setSelected(alliances[0][0]);
                    break;
            }

            c.gridx = 2;
            switch(player)
            {
                case 0: case 1:
                    alliancePanel.add(allianceArray[0][2], c);
                    allianceArray[0][2].setSelected(alliances[0][2]);
                    break;
                case 2: case 3:
                    alliancePanel.add(allianceArray[0][1], c);
                    allianceArray[0][1].setSelected(alliances[0][1]);
                    break;
            }

            c.gridx = 3;
            switch(player)
            {
                case 0: case 1: case 2:
                    alliancePanel.add(allianceArray[0][3], c);
                    allianceArray[0][3].setSelected(alliances[0][3]);
                    break;
                case 3:
                    alliancePanel.add(allianceArray[0][2], c);
                    allianceArray[0][2].setSelected(alliances[0][2]);
                    break;
            }

            c.gridx = 1;
            c.gridy = 2;
            switch(player)
            {
                case 0:
                    alliancePanel.add(allianceArray[1][1], c);
                    allianceArray[1][1].setSelected(alliances[1][1]);
                    break;
                case 1: case 2: case 3:
                    alliancePanel.add(allianceArray[1][0], c);
                    allianceArray[1][0].setSelected(alliances[1][0]);
                    break;
            }

            c.gridx = 2;
            switch(player)
            {
                case 0: case 1:
                    alliancePanel.add(allianceArray[1][2], c);
                    allianceArray[1][2].setSelected(alliances[1][2]);
                    break;
                case 2: case 3:
                    alliancePanel.add(allianceArray[1][1], c);
                    allianceArray[1][1].setSelected(alliances[1][1]);
                    break;
            }

            c.gridx = 3;
            switch(player)
            {
                case 0: case 1: case 2:
                    alliancePanel.add(allianceArray[1][3], c);
                    allianceArray[1][3].setSelected(alliances[1][3]);
                    break;
                case 3:
                    alliancePanel.add(allianceArray[1][2], c);
                    allianceArray[1][2].setSelected(alliances[1][2]);
                    break;
            }

            c.gridx = 1;
            c.gridy = 3;
            switch(player)
            {
                case 0:
                    alliancePanel.add(allianceArray[2][1], c);
                    allianceArray[2][1].setSelected(alliances[2][1]);
                    break;
                case 1: case 2: case 3:
                    alliancePanel.add(allianceArray[2][0], c);
                    allianceArray[2][0].setSelected(alliances[2][0]);
                    break;
            }

            c.gridx = 2;
            switch(player)
            {
                case 0: case 1:
                    alliancePanel.add(allianceArray[2][2], c);
                    allianceArray[2][2].setSelected(alliances[2][2]);
                    break;
                case 2: case 3:
                    alliancePanel.add(allianceArray[2][1], c);
                    allianceArray[2][1].setSelected(alliances[2][1]);
                    break;
            }

            c.gridx = 3;
            switch(player)
            {
                case 0: case 1: case 2:
                    alliancePanel.add(allianceArray[2][3], c);
                    allianceArray[2][3].setSelected(alliances[2][3]);
                    break;
                case 3:
                    alliancePanel.add(allianceArray[2][2], c);
                    allianceArray[2][2].setSelected(alliances[2][2]);
                    break;
            }

            c.gridx = 1;
            c.gridy = 4;
            switch(player)
            {
                case 0:
                    alliancePanel.add(allianceArray[3][1], c);
                    allianceArray[3][1].setSelected(alliances[3][1]);
                    break;
                case 1: case 2: case 3:
                    alliancePanel.add(allianceArray[3][0], c);
                    allianceArray[3][0].setSelected(alliances[3][0]);
                    break;
            }

            c.gridx = 2;
            switch(player)
            {
                case 0: case 1:
                    alliancePanel.add(allianceArray[3][2], c);
                    allianceArray[3][2].setSelected(alliances[3][2]);
                    break;
                case 2: case 3:
                    alliancePanel.add(allianceArray[3][1], c);
                    allianceArray[3][1].setSelected(alliances[3][1]);
                    break;
            }

            c.gridx = 3;
            switch(player)
            {
                case 0: case 1: case 2:
                    alliancePanel.add(allianceArray[3][3], c);
                    allianceArray[3][3].setSelected(alliances[3][3]);
                    break;
                case 3:
                    alliancePanel.add(allianceArray[3][2], c);
                    allianceArray[3][2].setSelected(alliances[3][2]);
                    break;
            }

            c.gridx = 1;
            c.gridy = 5;
            switch(player)
            {
                case 0:
                    alliancePanel.add(allianceArray[4][1], c);
                    allianceArray[4][1].setSelected(alliances[4][1]);
                    break;
                case 1: case 2: case 3:
                    alliancePanel.add(allianceArray[4][0], c);
                    allianceArray[4][0].setSelected(alliances[4][0]);
                    break;
            }

            c.gridx = 2;
                switch(player)
                {
                    case 0: case 1:
                        alliancePanel.add(allianceArray[4][2], c);
                        allianceArray[4][2].setSelected(alliances[4][2]);
                        break;
                    case 2: case 3:
                        alliancePanel.add(allianceArray[4][1], c);
                        allianceArray[4][1].setSelected(alliances[4][1]);
                        break;
                }

            c.gridx = 3;
            switch(player)
            {
                case 0: case 1: case 2:
                    alliancePanel.add(allianceArray[4][3], c);
                    allianceArray[4][3].setSelected(alliances[4][3]);
                    break;
                case 3:
                    alliancePanel.add(allianceArray[4][2], c);
                    allianceArray[4][2].setSelected(alliances[4][2]);
                    break;
            }

            return alliancePanel;

    }



    /**
     * Paints the GUI, to be called at every game tick.
     */
    public void paint() {
        // Update focused player.
        int focusedPlayer;
        if (humanIdx == -1) {
            // If human is not playing, main view will be desired view corresponding to player index.
            focusedPlayer = ki.getFocusedPlayer();
        } else {
            // If human is playing, main view will be human view, and not true game state.
            focusedPlayer = humanIdx;
        }

        // Added for alliances
        playerNo = focusedPlayer;

        // Update all views
        for (int i = 0; i < views.length; i++) {
            if (views[i] != null) {  // Side views (i > 0) may be null if human is playing and side view not displayed.
                int pIdx = i - 1;
                if (i == 0) {
                    pIdx = focusedPlayer;
                }

                views[i].paint(game.getBoard(pIdx), game.getGameState().getBombLife());
            }
        }

        // Update avatar display panel.
        avatarDisplayPanel.paint(game.getAvatars(focusedPlayer));

        // If human player died, show full observability for the rest of the match. Allows main view switching.
        if (humanIdx > -1 && !avatarDisplayPanel.getAlive()[humanIdx]) {
            humanIdx = -1;
        }

        // Update game tick.
        appTick.setText("tick: " + game.getTick());

        if (VERBOSE) {
            System.out.println("[GUI] Focused player: " + focusedPlayer);
        }
    }
}
