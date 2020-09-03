package utils;

import Message.MessageManager;
import core.Game;
import core.GameState;
import players.HumanPlayer;
import players.KeyController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;

import static utils.Types.*;

public class GUI extends JFrame {
    private JLabel appTick;
    private GameView[] views;
    private Game game;
    private GameState gs;
    private AvatarView avatarDisplayPanel;
    private KeyController ki;
    private int humanIdx;  // human player index in array of players
    private boolean displayPOHuman;  // if side views should be displayed when human is playing

    // Player and name of focused player
    private int playerNo = -1;
    private String playerName;

    // Label to show current game stage
    private JLabel allianceLabel;
    // New panel for alliance GUI
    private JPanel alliancePanel;
    // Array of 5x3 buttons showing current alliances
    private JToggleButton[][] allianceArray = new JToggleButton[5][3];
    // Buttons showing the 5 rules to make alliances on
    private JToggleButton[] rules = new JToggleButton[5];

    // Original game panel
    private JPanel mainPanel;
    // Original game panel
    private JPanel poPanel;

    // Alliances at each negotiation stage as 3D array X: For each player, Y: For each rule, Z: With which other player
    public boolean[][][] setAlliances = new boolean[4][5][3];
    public boolean[][][] receivedAlliances = new boolean[4][5][3];
    public boolean[][][] chosenAlliances = new boolean[4][5][3];

    // Remaining alliance proposals for the player
    private int proposalsLeft = NEGOTIATION_PROPOSAL_LIMIT;

    // Time left during each negotiation phase
    private int phaseTime1 = Types.NEGOTIATION_PHASE_ONE_LENGTH;
    private int phaseTime2 = Types.NEGOTIATION_PHASE_TWO_LENGTH;
    private int stage = 0;
    private int NextCollapse = COLLAPSE_START+1;

    // game paused for negotiation phases
    private boolean gamePause1 = false;
    private boolean gamePause2 = false;

    // Debug array for testing
    private boolean[][] testAlliance = {{false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}};

    // Agent icons for buttons
    private Icon agent0lo;
    private Icon agent0;
    private Icon agent1lo;
    private Icon agent1;
    private Icon agent2lo;
    private Icon agent2;
    private Icon agent3lo;
    private Icon agent3;

    // New Agent icons for proposals
    private Icon agent0slo;
    private Icon agent0s;
    private Icon agent1slo;
    private Icon agent1s;
    private Icon agent2slo;
    private Icon agent2s;
    private Icon agent3slo;
    private Icon agent3s;
    private Icon agentBlank;

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
        mainPanel = getMainPanel();

        // Add everything to side panel if we need it to be displayed
        poPanel = getPoPanel();

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
            /*else if (humanIdx > -1 && !displayPOHuman)
                // If a human is playing and we don't need to display the other PO views, leave them null
                break;*/

            views[i] = new GameView(game.getBoard(pIdx), cellSize);
        }
    }

    /**
     * Creates the main panel containing main view of true game state (or human if human playing).
     * Includes information about the game:
     *  - game tick or negotiation rules
     *  - game mode
     *  - game avatars status and names
     *  - new alliances panel
     * @return main panel.
     */
    @SuppressWarnings("Duplicates")
    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.SOUTH;
        c.weighty = 0;

        JLabel appTitle = new JLabel("Java-Pommertalk");
        Font textFont = new Font(appTitle.getFont().getName(), Font.BOLD, 18);
        appTitle.setFont(textFont);

        // restart game automatically when a game ends checkbox
        JCheckBox autoRestartBox = new JCheckBox("auto-restart on game end");
        autoRestartBox.setSize(1,1);
        textFont = new Font(appTitle.getFont().getName(), Font.PLAIN, 13);
        autoRestartBox.setSelected(true);
        autoRestartBox.setFocusable(false);
        autoRestartBox.setFont(textFont);
        autoRestartBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED)
                    game.runAgain = true;
                else
                    game.runAgain = false;
            }
        });

        // Now unused mode label as Pommertalk different from original game modes
        JLabel modeLabel = new JLabel("game mode: " + game.getGameMode());
        textFont = new Font(appTitle.getFont().getName(), Font.PLAIN, 16);
        modeLabel.setFont(textFont);

        appTick = new JLabel("tick: 0");
        appTick.setFont(textFont);

        // Names of players
        JLabel nameLabel = new JLabel("<html><font color=rgb(0,100,100)>SPEEDY&nbsp&nbsp&nbsp&nbsp&nbsp</font><font color=rgb(200,100,100)>SHADOW&nbsp&nbsp&nbsp&nbsp</font><font color= rgb(90,150,90)>BASHFUL&nbsp&nbsp&nbsp&nbsp</font><font color =rgb(160,160,80)>POKEY&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</font></html>");
        textFont = new Font(appTitle.getFont().getName(), Font.PLAIN, 13);
        nameLabel.setFont(textFont);

        avatarDisplayPanel = new AvatarView(game.getAvatars(-1));

        // Add everything to main panel
        mainPanel.add(appTitle, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 0)), c);
        c.gridy++;
        mainPanel.add(appTick, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 0)), c);
        c.gridy++;
        mainPanel.add(autoRestartBox, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 0)), c);
        c.gridy++;
        mainPanel.add(nameLabel, c);
        c.gridy++;
        mainPanel.add(avatarDisplayPanel, c);
        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 0)), c);
        c.gridy++;
        mainPanel.add(views[0], c);

        c.gridy++;
        mainPanel.add(Box.createRigidArea(new Dimension(0, 0)), c);
        c.gridy++;

        // Display alliance panel if selected or a human player
        if(playerNo == -1)
            alliancePanel = getAlliancePanel(0, chosenAlliances[0]);
        else
            alliancePanel = getAlliancePanel(playerNo, chosenAlliances[playerNo]);

        mainPanel.add(alliancePanel, c);

        alliancePanel.setVisible(false);

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
        else if (humanIdx > -1 && !displayPOHuman) {
            poPanel = new JPanel();
            poPanel.setLayout(new BoxLayout(poPanel, BoxLayout.Y_AXIS));
            poPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            for (int i = 1; i < views.length; i++) {
                poPanel.add(Box.createRigidArea(new Dimension(CELL_SIZE_PO*BOARD_SIZE, CELL_SIZE_PO*BOARD_SIZE)));
                poPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        return poPanel;
    }


    /**
     * Creates Alliances Panel which is added to main panel
     */
    private JPanel getAlliancePanel(int player, boolean[][] alliances) {

        JPanel alliancePanel = new JPanel();

        // Controls layout and settings for each component added
        alliancePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Label changes with instructions for each stage of negotiation
        allianceLabel = new JLabel("current alliances: player " + (player+1));
        Font textFont = new Font(allianceLabel.getFont().getName(), Font.PLAIN, 16);
        allianceLabel.setFont(textFont);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        alliancePanel.add(allianceLabel, c);

        // 5 different possible alliance rules added
        JToggleButton rule1 = new JToggleButton("Alliance");
        rule1.setFocusable(false);
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 60;
        alliancePanel.add(rule1, c);

        JToggleButton rule2 = new JToggleButton("Shared vision");
        rule2.setFocusable(false);
        c.ipadx = 25;
        c.gridy = 2;
        alliancePanel.add(rule2, c);

        JToggleButton rule3 = new JToggleButton("No bomb placing");
        rule3.setFocusable(false);
        c.ipadx = 0;
        c.gridy = 3;
        alliancePanel.add(rule3, c);

        JToggleButton rule4 = new JToggleButton("No bomb kicking");
        rule4.setFocusable(false);
        c.gridy = 4;
        alliancePanel.add(rule4, c);

        JToggleButton rule5 = new JToggleButton("Stay apart");
        rule5.setFocusable(false);
        c.ipadx =50;
        c.gridy = 5;
        alliancePanel.add(rule5, c);

        rules[0] = rule1;
        rules[1] = rule2;
        rules[2] = rule3;
        rules[3] = rule4;
        rules[4] = rule5;

        // Button icons
        agent0lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0lo.png"));
        agent0 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0.png"));
        agent1lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1lo.png"));
        agent1 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1.png"));
        agent2lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2lo.png"));
        agent2 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2.png"));
        agent3lo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3lo.png"));
        agent3 = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3.png"));

        // Alternate pink background icons
        agent0slo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0los.png"));
        agent0s = new ImageIcon(ImageIO.GetInstance().getImage("img/agent0s.png"));
        agent1slo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1los.png"));
        agent1s = new ImageIcon(ImageIO.GetInstance().getImage("img/agent1s.png"));
        agent2slo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2los.png"));
        agent2s = new ImageIcon(ImageIO.GetInstance().getImage("img/agent2s.png"));
        agent3slo = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3los.png"));
        agent3s = new ImageIcon(ImageIO.GetInstance().getImage("img/agent3s.png"));

        agentBlank = new ImageIcon(ImageIO.GetInstance().getImage("img/agentBlank.png"));

        // Assign button icons according to which is focused player since it can make alliances
        // with the 3 other players
        for(int i = 0; i < allianceArray.length; i++)
        {
                switch(player)
                {
                    case 0:
                        allianceArray[i][0] = new JToggleButton("", agent1lo);
                        allianceArray[i][0].setSelectedIcon(agent1);
                        allianceArray[i][0].setDisabledSelectedIcon(agent1);
                        allianceArray[i][0].setDisabledIcon(agent1lo);
                        allianceArray[i][0].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][1] = new JToggleButton("", agent2lo);
                        allianceArray[i][1].setSelectedIcon(agent2);
                        allianceArray[i][1].setDisabledSelectedIcon(agent2);
                        allianceArray[i][1].setDisabledIcon(agent2lo);
                        allianceArray[i][1].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][2] = new JToggleButton("", agent3lo);
                        allianceArray[i][2].setSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledIcon(agent3lo);
                        allianceArray[i][2].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 1:
                        allianceArray[i][0] = new JToggleButton("", agent0lo);
                        allianceArray[i][0].setSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledIcon(agent0lo);
                        allianceArray[i][0].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][1] = new JToggleButton("", agent2lo);
                        allianceArray[i][1].setSelectedIcon(agent2);
                        allianceArray[i][1].setDisabledSelectedIcon(agent2);
                        allianceArray[i][1].setDisabledIcon(agent2lo);
                        allianceArray[i][1].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][2] = new JToggleButton("", agent3lo);
                        allianceArray[i][2].setSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledIcon(agent3lo);
                        allianceArray[i][2].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 2:
                        allianceArray[i][0] = new JToggleButton("", agent0lo);
                        allianceArray[i][0].setSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledIcon(agent0lo);
                        allianceArray[i][0].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][1] = new JToggleButton("", agent1lo);
                        allianceArray[i][1].setSelectedIcon(agent1);
                        allianceArray[i][1].setDisabledSelectedIcon(agent1);
                        allianceArray[i][1].setDisabledIcon(agent1lo);
                        allianceArray[i][1].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][2] = new JToggleButton("", agent3lo);
                        allianceArray[i][2].setSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledSelectedIcon(agent3);
                        allianceArray[i][2].setDisabledIcon(agent3lo);
                        allianceArray[i][2].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                    case 3:
                        allianceArray[i][0] = new JToggleButton("", agent0lo);
                        allianceArray[i][0].setSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledSelectedIcon(agent0);
                        allianceArray[i][0].setDisabledIcon(agent0lo);
                        allianceArray[i][0].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][1] = new JToggleButton("", agent1lo);
                        allianceArray[i][1].setSelectedIcon(agent1);
                        allianceArray[i][1].setDisabledSelectedIcon(agent1);
                        allianceArray[i][1].setDisabledIcon(agent1lo);
                        allianceArray[i][1].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        allianceArray[i][2] = new JToggleButton("", agent2lo);
                        allianceArray[i][2].setSelectedIcon(agent2);
                        allianceArray[i][2].setDisabledSelectedIcon(agent2);
                        allianceArray[i][2].setDisabledIcon(agent2lo);
                        allianceArray[i][2].addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                AbstractButton abstractButton = (AbstractButton) e.getSource();

                                boolean selected = abstractButton.getModel().isSelected();
                            }
                        });
                        break;
                }
        }
            // Add buttons to panel in a 5x3 grid
            c.ipadx = 0;
            c.gridx = 1;
            c.gridy = 1;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.fill = GridBagConstraints.BOTH;
            alliancePanel.add(allianceArray[0][0], c);
            allianceArray[0][0].setSelected(alliances[0][0]);

            c.gridx = 2;
            alliancePanel.add(allianceArray[0][1], c);
            allianceArray[0][1].setSelected(alliances[0][1]);

            c.gridx = 3;
            alliancePanel.add(allianceArray[0][2], c);
            allianceArray[0][2].setSelected(alliances[0][2]);

            c.gridx = 1;
            c.gridy = 2;
            alliancePanel.add(allianceArray[1][0], c);
            allianceArray[1][0].setSelected(alliances[1][0]);

            c.gridx = 2;
            alliancePanel.add(allianceArray[1][1], c);
            allianceArray[1][1].setSelected(alliances[1][1]);

            c.gridx = 3;
            alliancePanel.add(allianceArray[1][2], c);
            allianceArray[1][2].setSelected(alliances[1][2]);

            c.gridx = 1;
            c.gridy = 3;
            alliancePanel.add(allianceArray[2][0], c);
            allianceArray[2][0].setSelected(alliances[2][0]);

            c.gridx = 2;
            alliancePanel.add(allianceArray[2][1], c);
            allianceArray[2][1].setSelected(alliances[2][1]);

            c.gridx = 3;
            alliancePanel.add(allianceArray[2][2], c);
            allianceArray[2][2].setSelected(alliances[2][2]);

            c.gridx = 1;
            c.gridy = 4;
            alliancePanel.add(allianceArray[3][0], c);
            allianceArray[3][0].setSelected(alliances[3][0]);

            c.gridx = 2;
            alliancePanel.add(allianceArray[3][1], c);
            allianceArray[3][1].setSelected(alliances[3][1]);

            c.gridx = 3;
            alliancePanel.add(allianceArray[3][2], c);
            allianceArray[3][2].setSelected(alliances[3][2]);

            c.gridx = 1;
            c.gridy = 5;
            alliancePanel.add(allianceArray[4][0], c);
            allianceArray[4][0].setSelected(alliances[4][0]);

            c.gridx = 2;
            alliancePanel.add(allianceArray[4][1], c);
            allianceArray[4][1].setSelected(alliances[4][1]);

            c.gridx = 3;
            alliancePanel.add(allianceArray[4][2], c);
            allianceArray[4][2].setSelected(alliances[4][2]);

            // Assign keyboard rules for moving between buttons
            // Rules are different for each negotiation phase when different buttons are enabled
            for (int i = 0; i < allianceArray.length; i++) {
                for (int j = 0; j < allianceArray[i].length; j++) {
                    final int curRow = i;
                    final int curCol = j;
                    allianceArray[i][j].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(game.getPhase() == GAME_PHASE.NEGOTIATION_ONE || gamePause1 == true) {
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_UP:
                                    if (curRow > 0) {
                                        if (allianceArray[curRow - 1][curCol].isEnabled()) {
                                            allianceArray[curRow - 1][curCol].requestFocus();
                                            rules[curRow - 1].setSelected(true);
                                            rules[curRow].setSelected(false);
                                        }
                                    }
                                    break;
                                case KeyEvent.VK_DOWN:
                                    if (curRow < allianceArray.length - 1) {
                                        if (allianceArray[curRow + 1][curCol].isEnabled()) {
                                            allianceArray[curRow + 1][curCol].requestFocus();
                                            rules[curRow + 1].setSelected(true);
                                            rules[curRow].setSelected(false);
                                        }
                                    }
                                    break;
                                case KeyEvent.VK_LEFT:
                                    if (curCol > 0)
                                        allianceArray[curRow][curCol - 1].requestFocus();
                                    break;
                                case KeyEvent.VK_RIGHT:
                                    if (curCol < allianceArray[curRow].length - 1)
                                        allianceArray[curRow][curCol + 1].requestFocus();
                                    break;
                                case KeyEvent.VK_SPACE:
                                    if (!allianceArray[curRow][curCol].isSelected() && proposalsLeft > 0)
                                        proposalsLeft--;
                                    else if (allianceArray[curRow][curCol].isSelected())
                                    {
                                        if(proposalsLeft < NEGOTIATION_PROPOSAL_LIMIT)
                                        proposalsLeft++;
                                    }
                                    else if (proposalsLeft == 0)
                                        allianceArray[curRow][curCol].setSelected(!allianceArray[curRow][curCol].isSelected());
                                    break;
                                case KeyEvent.VK_P:
                                    gamePause1 = !gamePause1;
                                    game.pauseGame(gamePause1);
                                    break;
                                default:
                                    break;
                            }
                        }
                        else if(game.getPhase() == GAME_PHASE.NEGOTIATION_TWO || gamePause2 == true)
                        {
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_UP:
                                case KeyEvent.VK_LEFT:
                                    allianceArray[curRow][curCol].transferFocusBackward();
                                    break;
                                case KeyEvent.VK_DOWN: case KeyEvent.VK_RIGHT:
                                    allianceArray[curRow][curCol].transferFocus();
                                    break;
                                case KeyEvent.VK_P:
                                    gamePause2 = !gamePause2;
                                    game.pauseGame(gamePause2);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });

            }
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
        switch(focusedPlayer) {
            case 0:
                playerName = "SPEEDY";
                break;
            case 1:
                playerName = "SHADOW";
                break;
            case 2:
                playerName = "BASHFUL";
                break;
            case 3:
                playerName = "POKEY";
                break;
        }

        // Update Game state
        gs = game.getGameState();

        // Normal state is standard game play as shown by grey background colour
        if(game.getPhase() == GAME_PHASE.NORMAL)
        {
            // set background colour
            mainPanel.setBackground(new Color(238,238,238));
            alliancePanel.setBackground(new Color(238,238,238));

            // Return focus to game instead of buttons so can move player
            this.requestFocus();

            // Reset negotiation times
            phaseTime1 = Types.NEGOTIATION_PHASE_ONE_LENGTH;
            phaseTime2 = Types.NEGOTIATION_PHASE_ONE_LENGTH;

            if(focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == true)
                allianceLabel.setText("current alliances: " + playerName);
            else if(focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == false)
                allianceLabel.setText(playerName + " is dead but not forgotten");


            // Highlight allowances and rules in play for current round
            if(focusedPlayer > -1)
            {
                for(int i = 0; i < allianceArray.length; i++)
                {
                    for(int j = 0; j < allianceArray[0].length; j++)
                    {
                        allianceArray[i][j].setSelected(chosenAlliances[focusedPlayer][i][j]);
                        allianceArray[i][j].setEnabled(false);
                    }
                }
            }
            for(int i = 0; i < rules.length; i++)
            {
                if(!allianceArray[i][0].isSelected() && !allianceArray[i][1].isSelected() && !allianceArray[i][2].isSelected())
                {
                    rules[i].setSelected(false);
                }
                else
                    rules[i].setSelected(true);
            }


            // playerNo added for alliances to have a global variable of the focussed player
            // Also allows only updating alliance panel if focused player has changed
            // If ai only game display each player's chosen alliances
            if(playerNo != focusedPlayer || humanIdx > -1) {
                playerNo = focusedPlayer;
                if (focusedPlayer == -1)
                    alliancePanel.setVisible(false);
                else {
                    alliancePanel.setVisible(true);

                    // Display relevant opponents for each focused player
                    switch (focusedPlayer) {
                        case 0:
                            for(int i = 0; i < allianceArray.length; i++)
                            {
                                allianceArray[i][0].setIcon(agent1lo);
                                allianceArray[i][0].setSelectedIcon(agent1);
                                allianceArray[i][0].setDisabledSelectedIcon(agent1);
                                allianceArray[i][0].setDisabledIcon(agent1lo);
                                allianceArray[i][0].setSelected(chosenAlliances[0][i][0]);

                                allianceArray[i][1].setIcon(agent2lo);
                                allianceArray[i][1].setSelectedIcon(agent2);
                                allianceArray[i][1].setDisabledSelectedIcon(agent2);
                                allianceArray[i][1].setDisabledIcon(agent2lo);
                                allianceArray[i][1].setSelected(chosenAlliances[0][i][1]);

                                allianceArray[i][2].setIcon(agent3lo);
                                allianceArray[i][2].setSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledIcon(agent3lo);
                                allianceArray[i][2].setSelected(chosenAlliances[0][i][2]);
                            }
                            break;
                        case 1:
                            for(int i = 0; i < allianceArray.length; i++)
                            {
                                allianceArray[i][0].setIcon(agent0lo);
                                allianceArray[i][0].setSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledIcon(agent0lo);
                                allianceArray[i][0].setSelected(chosenAlliances[1][i][0]);

                                allianceArray[i][1].setIcon(agent2lo);
                                allianceArray[i][1].setSelectedIcon(agent2);
                                allianceArray[i][1].setDisabledSelectedIcon(agent2);
                                allianceArray[i][1].setDisabledIcon(agent2lo);
                                allianceArray[i][1].setSelected(chosenAlliances[1][i][1]);

                                allianceArray[i][2].setIcon(agent3lo);
                                allianceArray[i][2].setSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledIcon(agent3lo);
                                allianceArray[i][2].setSelected(chosenAlliances[1][i][2]);
                            }
                            break;
                        case 2:
                            for(int i = 0; i < allianceArray.length; i++)
                            {
                                allianceArray[i][0].setIcon(agent0lo);
                                allianceArray[i][0].setSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledIcon(agent0lo);
                                allianceArray[i][0].setSelected(chosenAlliances[2][i][0]);

                                allianceArray[i][1].setIcon(agent1lo);
                                allianceArray[i][1].setSelectedIcon(agent1);
                                allianceArray[i][1].setDisabledSelectedIcon(agent1);
                                allianceArray[i][1].setDisabledIcon(agent1lo);
                                allianceArray[i][1].setSelected(chosenAlliances[2][i][1]);

                                allianceArray[i][2].setIcon(agent3lo);
                                allianceArray[i][2].setSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledSelectedIcon(agent3);
                                allianceArray[i][2].setDisabledIcon(agent3lo);
                                allianceArray[i][2].setSelected(chosenAlliances[2][i][2]);
                            }
                            break;
                        case 3:
                            for(int i = 0; i < allianceArray.length; i++)
                            {
                                allianceArray[i][0].setIcon(agent0lo);
                                allianceArray[i][0].setSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledSelectedIcon(agent0);
                                allianceArray[i][0].setDisabledIcon(agent0lo);
                                allianceArray[i][0].setSelected(chosenAlliances[3][i][0]);

                                allianceArray[i][1].setIcon(agent1lo);
                                allianceArray[i][1].setSelectedIcon(agent1);
                                allianceArray[i][1].setDisabledSelectedIcon(agent1);
                                allianceArray[i][1].setDisabledIcon(agent1lo);
                                allianceArray[i][1].setSelected(chosenAlliances[3][i][1]);

                                allianceArray[i][2].setIcon(agent2lo);
                                allianceArray[i][2].setSelectedIcon(agent2);
                                allianceArray[i][2].setDisabledSelectedIcon(agent2);
                                allianceArray[i][2].setDisabledIcon(agent2lo);
                                allianceArray[i][2].setSelected(chosenAlliances[3][i][2]);
                            }
                            break;

                    }
                }
            }

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

            // Time of next screen collapse
            NextCollapse = COLLAPSE_START+1 + stage*COLLAPSE_STEP;

            // Update game tick.
            //appTick.setText("tick: " + game.getTick() + ". next round at " + NextCollapse);
            appTick.setText("ticks until screen collapse: " + (NextCollapse-game.getTick()));
            if(NextCollapse-game.getTick() < 0)
                appTick.setText("STARTING NEGOTIATION PHASE");

            if (VERBOSE) {
                System.out.println("[GUI] Focused player: " + focusedPlayer);
            }


        }

        // First negotiation stage of selecting alliances to propose. Yellow background.
        else if(game.getPhase() == GAME_PHASE.NEGOTIATION_ONE)
        {
            // set background colour
            mainPanel.setBackground(new Color(255,250,200));
            alliancePanel.setBackground(new Color(255,250,200));

            // Move focus to buttons for human player
            if (humanIdx > -1)
            {
                // Perform on first entering phase
                if(phaseTime1 == NEGOTIATION_PHASE_ONE_LENGTH)
                {
                    proposalsLeft = NEGOTIATION_PROPOSAL_LIMIT;
                    // Enable buttons for player to press
                    for(int i = 0; i < allianceArray.length; i++)
                    {
                        for(int j = 0; j < allianceArray[i].length; j++)
                        {
                            rules[i].setEnabled(true);
                            rules[i].setSelected(false);
                            allianceArray[i][j].setEnabled(true);
                            allianceArray[i][j].setFocusable(true);
                            // Keep alliances from previous round
                            /*if(allianceArray[i][j].isSelected())
                                proposalsLeft--;*/
                            // Don't keep alliances from previous round
                            allianceArray[i][j].setSelected(false);
                        }
                    }

                    allianceArray[0][0].requestFocus();
                    rules[0].setSelected(true);

                    allianceLabel.setText("request MAX " + NEGOTIATION_PROPOSAL_LIMIT + " alliances: " + playerName);
                    appTick.setText(" PRESS 'p' TO CONTINUE.");

                    // Pause game
                    gamePause1 = true;
                    game.pauseGame(gamePause1);
                }
                else if(phaseTime1 < NEGOTIATION_PHASE_ONE_LENGTH)
                {
                    appTick.setText("NEXT PHASE IN " + phaseTime1/10);
                }

            }

            // For ai game get requested alliances from message system
            else if (humanIdx == -1) {

                // Clear buttons on first entering phase
                if (phaseTime1 == NEGOTIATION_PHASE_ONE_LENGTH) {
                    for (int i = 0; i < allianceArray.length; i++) {
                        for (int j = 0; j < allianceArray[i].length; j++) {
                            allianceArray[i][j].setSelected(false);
                            rules[i].setSelected(false);
                        }
                    }
                }

                if (focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == true)
                    allianceLabel.setText(playerName + " is requesting alliances");
                else if (focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == false)
                    allianceLabel.setText(playerName + " is dead but not forgotten");

                appTick.setText("MAKING PROPOSALS IN " + phaseTime1 / 10);

                // Fetch all proposed ai alliances and show using buttons
                if (NEGOTIATION_PHASE_ONE_LENGTH - phaseTime1 == 15) {
                    // Get each ai proposed alliances
                    MessageManager ms = gs.getMessageManager();
                    setAlliances = ms.proposalAsBool();
                    //}
                    if (focusedPlayer > -1) {
                        for (int i = 0; i < setAlliances[focusedPlayer].length; i++) {
                            for (int j = 0; j < setAlliances[focusedPlayer][i].length; j++) {
                                if (setAlliances[focusedPlayer][i][j] == true) {
                                    allianceArray[i][j].setSelected(true);
                                    rules[i].setSelected(true);
                                } else
                                    allianceArray[i][j].setSelected(false);
                            }
                        }
                        for (int i = 0; i < rules.length; i++) {
                            if (!allianceArray[i][0].isSelected() && !allianceArray[i][1].isSelected() && !allianceArray[i][2].isSelected()) {
                                rules[i].setSelected(false);
                            }
                        }
                    }

                }
            }

            //Reduce time of negotiation phase
            phaseTime1--;

            // return focus to main game and send alliances just before timer ends
            if(phaseTime1 == 1 && humanIdx > -1)
            {
                this.requestFocus();

                // Finalise requested alliances
                for(int i = 0; i < allianceArray.length; i++)
                {
                    for(int j = 0; j < allianceArray[0].length; j++)
                    {
                        if(allianceArray[i][j].isSelected())
                        {
                            setAlliances[playerNo][i][j] = true;
                        }
                        else
                            setAlliances[playerNo][i][j] = false;
                    }
                }
                // Send proposed alliances to message system
                MessageManager ms = gs.getMessageManager();
                ms.boolPropToMessage(setAlliances);
                receivedAlliances = ms.receivedPropToBool();
            }

        }

        // Negotiation phase 2. Green background
        else if(game.getPhase() == GAME_PHASE.NEGOTIATION_TWO) {
            // set background colour
            mainPanel.setBackground(new Color(185, 210, 185));
            alliancePanel.setBackground(new Color(185, 210, 185));

            // Allow human player to choose from received alliances
            if (humanIdx > -1) {
                //allianceLabel.setText("accept/reject alliances: " + playerName);
                //appTick.setText("SELECT FROM PROPOSALS IN " + phaseTime2/10);

                // Perform on first entering phase
                if (phaseTime2 == NEGOTIATION_PHASE_TWO_LENGTH) {

                    //DEBUG
                    //receivedAlliances[playerNo] = testAlliance;

                    for (int i = allianceArray.length - 1; i >= 0; i--) {
                        for (int j = allianceArray[i].length - 1; j >= 0; j--) {
                            if (receivedAlliances[playerNo][i][j] == true) {

                                // Display proposals to select from for each player
                                switch (focusedPlayer) {
                                    case 0:
                                        switch (j) {
                                            case 0:
                                                allianceArray[i][j].setIcon(agent1slo);
                                                allianceArray[i][j].setSelectedIcon(agent1s);
                                                break;
                                            case 1:
                                                allianceArray[i][j].setIcon(agent2slo);
                                                allianceArray[i][j].setSelectedIcon(agent2s);
                                                break;
                                            case 2:
                                                allianceArray[i][j].setIcon(agent3slo);
                                                allianceArray[i][j].setSelectedIcon(agent3s);
                                                break;
                                        }
                                        break;
                                    case 1:
                                        switch (j) {
                                            case 0:
                                                allianceArray[i][j].setIcon(agent0slo);
                                                allianceArray[i][j].setSelectedIcon(agent0s);
                                                break;
                                            case 1:
                                                allianceArray[i][j].setIcon(agent2slo);
                                                allianceArray[i][j].setSelectedIcon(agent2s);
                                                break;
                                            case 2:
                                                allianceArray[i][j].setIcon(agent3slo);
                                                allianceArray[i][j].setSelectedIcon(agent3s);
                                                break;
                                        }
                                        break;
                                    case 2:
                                        switch (j) {
                                            case 0:
                                                allianceArray[i][j].setIcon(agent0slo);
                                                allianceArray[i][j].setSelectedIcon(agent0s);
                                                break;
                                            case 1:
                                                allianceArray[i][j].setIcon(agent1slo);
                                                allianceArray[i][j].setSelectedIcon(agent1s);
                                                break;
                                            case 2:
                                                allianceArray[i][j].setIcon(agent3slo);
                                                allianceArray[i][j].setSelectedIcon(agent3s);
                                                break;
                                        }
                                        break;
                                    case 3:
                                        switch (j) {
                                            case 0:
                                                allianceArray[i][j].setIcon(agent0slo);
                                                allianceArray[i][j].setSelectedIcon(agent0s);
                                                break;
                                            case 1:
                                                allianceArray[i][j].setIcon(agent1slo);
                                                allianceArray[i][j].setSelectedIcon(agent1s);
                                                break;
                                            case 2:
                                                allianceArray[i][j].setIcon(agent2slo);
                                                allianceArray[i][j].setSelectedIcon(agent2s);
                                                break;
                                        }
                                        break;
                                }
                                // End of switch statement to possibly remove

                                allianceArray[i][j].setSelected(false);
                                allianceArray[i][j].setEnabled(true);
                                allianceArray[i][j].requestFocus();
                                rules[i].setSelected(false);
                            } else {
                                allianceArray[i][j].setDisabledIcon(agentBlank);
                                allianceArray[i][j].setSelected(false);
                                allianceArray[i][j].setEnabled(false);
                                rules[i].setSelected(false);
                            }
                        }
                    }

                    if(receivedAlliances[playerNo] == testAlliance)
                    {
                        allianceLabel.setText("no alliances received: " + playerName);
                    }
                    else
                    {
                        allianceLabel.setText("accept/reject alliances: " + playerName);
                        appTick.setText("PRESS 'p' TO CONTINUE.");

                        // Pause game
                        gamePause2 = true;
                        game.pauseGame(gamePause2);
                    }


                } else if (phaseTime2 < NEGOTIATION_PHASE_TWO_LENGTH) {
                    appTick.setText("REJOINING GAME IN " + phaseTime2 / 10);
                }



                // Flash icons to show proposals
                // Removed now game paused as game ticks no longer count down during negotiation
               /* for(int i = allianceArray.length -1; i >= 0 ; i--) {
                    for (int j = allianceArray[i].length - 1; j >= 0; j--) {
                        if (!allianceArray[i][j].isSelected() && phaseTime2 % 4 == 0) {
                            switch (focusedPlayer) {
                                case 0:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent1lo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent2lo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3lo);
                                            break;
                                    }
                                    break;
                                case 1:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0lo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent2lo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3lo);
                                            break;
                                    }
                                    break;
                                case 2:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0lo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent1lo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3lo);
                                            break;
                                    }
                                    break;
                                case 3:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0lo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent1lo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent2lo);
                                            break;
                                    }
                                    break;
                            }
                        } else if (!allianceArray[i][j].isSelected()) {
                            switch (focusedPlayer) {
                                case 0:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent1slo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent2slo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3slo);
                                            break;
                                    }
                                    break;
                                case 1:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0slo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent2slo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3slo);
                                            break;
                                    }
                                    break;
                                case 2:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0slo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent1slo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent3slo);
                                            break;
                                    }
                                    break;
                                case 3:
                                    switch (j) {
                                        case 0:
                                            allianceArray[i][j].setIcon(agent0slo);
                                            break;
                                        case 1:
                                            allianceArray[i][j].setIcon(agent1slo);
                                            break;
                                        case 2:
                                            allianceArray[i][j].setIcon(agent2slo);
                                            break;
                                    }
                                    break;
                            }
                        }
                    }
                }*/

            }
            // For ai game or after human player has died show selected alliances
            else if(humanIdx == -1) {
                // Clear buttons on first entering phase
                if (phaseTime2 == NEGOTIATION_PHASE_TWO_LENGTH) {
                    for (int i = 0; i < allianceArray.length; i++) {
                        for (int j = 0; j < allianceArray[i].length; j++) {
                            allianceArray[i][j].setSelected(false);
                            rules[i].setSelected(false);
                        }
                    }
                }

                appTick.setText("SELECTING FROM PROPOSALS IN " + phaseTime2 / 10);

                if (focusedPlayer > -1) {
                    if (focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == true)
                        allianceLabel.setText(playerName + " is picking alliances");
                    else if (focusedPlayer > -1 && avatarDisplayPanel.getAlive()[focusedPlayer] == false)
                        allianceLabel.setText(playerName + " is dead but not forgotten");

                    // Fetch chosen ai alliances and show using buttons
                    if (NEGOTIATION_PHASE_TWO_LENGTH - phaseTime2 == 15) {
                        // Get each ai proposed alliances
                        MessageManager ms = gs.getMessageManager();
                        chosenAlliances = ms.agreedPropToBool();
                        //}
                        for (int i = 0; i < chosenAlliances[focusedPlayer].length; i++) {
                            for (int j = 0; j < chosenAlliances[focusedPlayer][i].length; j++) {
                                if (chosenAlliances[focusedPlayer][i][j] == true) {
                                    allianceArray[i][j].setSelected(true);
                                    rules[i].setSelected(true);
                                } else
                                    allianceArray[i][j].setSelected(false);
                            }
                        }
                    }

                    }

                    // deselect rules with no ticks
                    for (int i = 0; i < rules.length; i++) {
                        if (!allianceArray[i][0].isSelected() && !allianceArray[i][1].isSelected() && !allianceArray[i][2].isSelected()) {
                            rules[i].setSelected(false);
                        }
                    }

            }

            // Reduce time of negotiation phase 2
            phaseTime2--;

            // return focus to game and send alliances just before timer ends
            if(phaseTime2 == 1)
            {
                stage++;
                this.requestFocus();

                // Initialise array for all players so not sending ai alliances from last round
                for(int k = 0; k < NUM_PLAYERS; k ++) {
                    for (int i = 0; i < allianceArray.length; i++) {
                        for (int j = 0; j < allianceArray[0].length; j++) {
                            chosenAlliances[k][i][j] = false;
                        }
                    }
                }

                if(humanIdx > -1)
                {
                    // Finalise chosen alliances
                    for(int i = 0; i < allianceArray.length; i++)
                    {
                        for(int j = 0; j < allianceArray[0].length; j++)
                        {
                            if(allianceArray[i][j].isSelected())
                            {
                                chosenAlliances[playerNo][i][j] = true;
                                rules[i].setSelected(true);
                            }
                            else
                                chosenAlliances[playerNo][i][j] = false;
                        }
                    }
                }

                // Agree next round alliances and send to message system
                MessageManager ms = gs.getMessageManager();
                ms.boolRespToMessage(chosenAlliances);
                chosenAlliances = ms.agreedPropToBool();

            }

        }
    }
}
