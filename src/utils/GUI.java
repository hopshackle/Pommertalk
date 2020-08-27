package utils;

import core.Game;
import core.GameState;
import players.HumanPlayer;
import players.KeyController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

    // Boolean for entering human negotation phase
    public boolean gamePaused;

    // Player to show GUI for
    private int playerNo = -1;

    // Which player following. Can also change description during each negotiation phase
    private JLabel allianceLabel;
    // Alliances Panel added below main game board
    private JPanel alliancePanel;
    // Array of 5x3 buttons showing current alliances
    private JToggleButton[][] allianceArray = new JToggleButton[5][3];
    // Buttons showing the 5 rules to make alliances on
    private JToggleButton[] rules = new JToggleButton[5];

    // Alliances at each negotiation stage as 3D array X: For each player, Y: For each rule, Z: With which other player
    public boolean[][][] setAlliances = new boolean[4][5][3];
    public boolean[][][] receivedAlliances = new boolean[4][5][3];
    public boolean[][][] chosenAlliances = new boolean[4][5][3];

    // Time left of each negotiation phase
    private int phaseTime1 = Types.NEGOTIATION_PHASE_ONE_LENGTH;
    private int phaseTime2 = Types.NEGOTIATION_PHASE_TWO_LENGTH;

    // Debug
    private boolean[][] testAlliance = {{false, true, false}, {true, false, false}, {false, false, false}, {false, false, false}, {false, false, true}};

    // Agent icons for buttons
    private Icon agent0lo;
    private Icon agent0;
    private Icon agent1lo;
    private Icon agent1;
    private Icon agent2lo;
    private Icon agent2;
    private Icon agent3lo;
    private Icon agent3;

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
        return poPanel;
    }


    /**
     * Creates Alliances Panel which is added onto main panel
     */
    private JPanel getAlliancePanel(int player, boolean[][] alliances) {

        JPanel alliancePanel = new JPanel();

        alliancePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        allianceLabel = new JLabel("current alliances: player " + (player+1));
        Font textFont = new Font(allianceLabel.getFont().getName(), Font.PLAIN, 16);
        allianceLabel.setFont(textFont);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        alliancePanel.add(allianceLabel, c);

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

        //JToggleButton allianceArray = new JToggleButton[5][4];

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

            for (int i = 0; i < allianceArray.length; i++) {
                for (int j = 0; j < allianceArray[i].length; j++) {
                    final int curRow = i;
                    final int curCol = j;
                    allianceArray[i][j].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(game.getGameState().getPhase() == GAME_PHASE.NEGOTIATION_ONE) {
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
                                default:
                                    break;
                            }
                        }
                        else if(game.getGameState().getPhase() == GAME_PHASE.NEGOTIATION_TWO)
                        {
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_UP:
                                case KeyEvent.VK_LEFT:
                                    allianceArray[curRow][curCol].transferFocusBackward();
                                    break;
                                case KeyEvent.VK_DOWN: case KeyEvent.VK_RIGHT:
                                    allianceArray[curRow][curCol].transferFocus();
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

        // Update Game state
        gs = game.getGameState();

        if(gs.getPhase() == GAME_PHASE.NORMAL)
        {
            // Return focus to game instead of buttons so can move player
            this.requestFocus();

            // Reset negotiation times
            phaseTime1 = Types.NEGOTIATION_PHASE_ONE_LENGTH;
            phaseTime2 = Types.NEGOTIATION_PHASE_ONE_LENGTH;

            allianceLabel.setText("current alliances: player " + (focusedPlayer + 1));

            for(int i = 0; i < allianceArray.length; i++)
            {
                for(int j = 0; j < allianceArray[0].length; j++)
                {
                    allianceArray[i][j].setEnabled(false);
                }
            }

            // playerNo added for alliances to have global variable of focussed player and see if focussed player changed
            // Only update alliance panel is this variable has changed
            // If ai only game display each player's chosen alliances
            if(playerNo != focusedPlayer || humanIdx > -1) {
                playerNo = focusedPlayer;
                if (focusedPlayer == -1)
                    alliancePanel.setVisible(false);
                else {
                    alliancePanel.setVisible(true);

                    allianceLabel.setText("current alliances: player " + (focusedPlayer + 1));
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

            // Update game tick.
            appTick.setText("tick: " + game.getTick());

            if (VERBOSE) {
                System.out.println("[GUI] Focused player: " + focusedPlayer);
            }


        }
        else if(gs.getPhase() == GAME_PHASE.NEGOTIATION_ONE)
        {
            // Move focus to buttons for human player
            if (humanIdx > -1)
            {
                // Perform on first entering phase
                if(phaseTime1 == NEGOTIATION_PHASE_ONE_LENGTH)
                {
                    // Enable buttons for player to press
                    for(int i = 0; i < allianceArray.length; i++)
                    {
                        for(int j = 0; j < allianceArray[j].length; j++)
                        {
                            rules[i].setEnabled(true);
                            allianceArray[i][j].setEnabled(true);
                            allianceArray[i][j].setFocusable(true);
                        }
                    }

                    alliancePanel.requestFocus();
                    allianceArray[0][0].requestFocusInWindow();
                    rules[0].setSelected(true);
                }



                allianceLabel.setText("request alliances " + phaseTime1/10 + ": player " + (focusedPlayer + 1));

                for(int i = 0; i < allianceArray.length; i++)
                {
                    for(int j = 0; j < allianceArray[0].length; j++)
                    {
                        if(allianceArray[i][j].isSelected())
                        {
                            setAlliances[playerNo][i][j] = true;
                        }
                    }
                }
            }

            // For ai game just display game phase
            else if (humanIdx == -1)
            {
                allianceLabel.setText("requesting alliances " + phaseTime1/10 + ": player " + (focusedPlayer + 1));
            }

            //Reduce time
            phaseTime1--;
        }
        else if(gs.getPhase() == GAME_PHASE.NEGOTIATION_TWO)
        {
            if(humanIdx > -1)
            {
                allianceLabel.setText("pick alliances " + phaseTime2/10 + ": player " + (focusedPlayer + 1));

                for(int k = 0; k < NUM_PLAYERS; k++)
                {
                    for(int i = 0; i < allianceArray.length; i++)
                    {
                        for(int j = 0; j < allianceArray[0].length; j++)
                        {
                            if(receivedAlliances[k][i][j] == true)
                            {
                                allianceArray[i][j].setSelected(true);
                                allianceArray[i][j].setEnabled(true);
                            }
                            else
                            {
                                allianceArray[i][j].setSelected(false);
                                allianceArray[i][j].setEnabled(false);
                            }
                        }
                    }
                }
            }
            else if(humanIdx == -1)
            {
                allianceLabel.setText("picking alliances " + phaseTime2/10 + ": player " + (focusedPlayer + 1));
            }

            for(int i = 0; i < allianceArray.length; i++)
            {
                for(int j = 0; j < allianceArray[0].length; j++)
                {
                    if(allianceArray[i][j].isSelected())
                    {
                        chosenAlliances[playerNo][i][j] = true;
                        rules[i].setSelected(true);
                    }
                }
            }
            for(int i = 0; i < rules.length; i++)
            {
                if(!allianceArray[i][0].isSelected() && !allianceArray[i][1].isSelected() && !allianceArray[i][2].isSelected())
                {
                    rules[i].setSelected(false);
                }
            }

            // Reduce time
            phaseTime2--;

        }
    }
}
