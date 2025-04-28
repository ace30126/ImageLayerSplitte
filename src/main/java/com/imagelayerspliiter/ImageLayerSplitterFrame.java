// ImageLayerSplitterFrame.java

package com.imagelayerspliiter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL; 
import javax.imageio.ImageIO;

public class ImageLayerSplitterFrame extends JFrame {

    private JLabel mainImageLabel;
    private JLabel redLayerLabel;
    private JLabel greenLayerLabel;
    private JLabel blueLayerLabel;
    private JLabel alphaLayerLabel;

    private JSlider redSlider;
    private JSlider greenSlider;
    private JSlider blueSlider;
    private JSlider alphaSlider;

    private BufferedImage originalImage; 

    private static final int LAYER_IMG_WIDTH = 150;
    private static final int LAYER_IMG_HEIGHT = 150;
    private static final int LAYER_PANEL_WIDTH = 170;
    private static final int LAYER_PANEL_HEIGHT = 220;

    public ImageLayerSplitterFrame() {
        setTitle("ImageLayerSplitter with Sliders");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // --- 프레임 아이콘 설정 ---
        // 클래스패스 루트에서 icon.png 로드 시도
        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            ImageIcon frameIcon = new ImageIcon(iconURL);
            setIconImage(frameIcon.getImage()); // JFrame의 아이콘으로 설정
        } else {
            // 아이콘 로드 실패 시 에러 메시지 출력 (콘솔)
            System.err.println("Warning: Could not load frame icon 'icon.png' from classpath.");
        }
        // --- 아이콘 설정 끝 ---


        // --- 중앙 이미지 표시 영역 ---
        mainImageLabel = new JLabel("이미지를 드래그 앤 드롭", SwingConstants.CENTER);
        // ... (mainImageLabel 나머지 설정 및 DropTarget) ...
        mainImageLabel.setBorder(new LineBorder(Color.GRAY));
        mainImageLabel.setPreferredSize(new Dimension(300, 300)); // 선호 크기 설정
        mainImageLabel.setDropTarget(new DropTarget(mainImageLabel, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY);
                        java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (fileList != null && !fileList.isEmpty()) {
                            File imageFile = fileList.get(0);
                            loadImage(imageFile);
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ImageLayerSplitterFrame.this, "파일 처리 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        add(mainImageLabel, BorderLayout.CENTER);


        // --- 하단 레이어 미리보기 및 슬라이더 영역 ---
        JPanel layerControlPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        layerControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        redLayerLabel = new JLabel();
        greenLayerLabel = new JLabel();
        blueLayerLabel = new JLabel();
        alphaLayerLabel = new JLabel();

        redSlider = createSlider();
        greenSlider = createSlider();
        blueSlider = createSlider();
        alphaSlider = createSlider();

        layerControlPanel.add(createLayerPanel("Red", redLayerLabel, redSlider));
        layerControlPanel.add(createLayerPanel("Green", greenLayerLabel, greenSlider));
        layerControlPanel.add(createLayerPanel("Blue", blueLayerLabel, blueSlider));
        layerControlPanel.add(createLayerPanel("Alpha", alphaLayerLabel, alphaSlider));

        add(layerControlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 슬라이더 생성 헬퍼 메소드
    private JSlider createSlider() {
        JSlider slider = new JSlider(0, 255, 255);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(false); // 숫자 레이블은 공간을 차지하므로 비활성화
        slider.setEnabled(false); // 초기에는 비활성화
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // 슬라이더 조정이 끝나면 업데이트 (getValueIsAdjusting() 사용)
                 if (!((JSlider)e.getSource()).getValueIsAdjusting()) {
                    updateLayers();
                 }
            }
        });
        return slider;
    }

    // 레이어 패널 생성 헬퍼 메소드
    private JPanel createLayerPanel(String title, JLabel imageLabel, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setPreferredSize(new Dimension(LAYER_PANEL_WIDTH, LAYER_PANEL_HEIGHT));

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(LAYER_IMG_WIDTH, LAYER_IMG_HEIGHT));
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(slider, BorderLayout.SOUTH);
        return panel;
    }


    private void loadImage(File file) {
        try {
            originalImage = ImageIO.read(file);
            if (originalImage != null) {
                int mainImgWidth = Math.min(originalImage.getWidth(), 300);
                int mainImgHeight = Math.min(originalImage.getHeight(), 300);
                Image scaledMainImage = originalImage.getScaledInstance(mainImgWidth, mainImgHeight, Image.SCALE_SMOOTH);
                mainImageLabel.setIcon(new ImageIcon(scaledMainImage));
                mainImageLabel.setText("");
                mainImageLabel.setPreferredSize(new Dimension(mainImgWidth, mainImgHeight));

                setSlidersEnabled(true);
                resetSliders();
                splitLayers(originalImage);

                pack();

            } else {
                clearLayersAndDisableSliders("이미지 로드 실패");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "이미지 로드 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            clearLayersAndDisableSliders("이미지 로드 오류");
        }
    }

    // 레이어 초기화 및 슬라이더 비활성화
    private void clearLayersAndDisableSliders(String message) {
        originalImage = null;
        mainImageLabel.setIcon(null);
        mainImageLabel.setText(message);
        mainImageLabel.setPreferredSize(new Dimension(300, 300));

        redLayerLabel.setIcon(null);
        greenLayerLabel.setIcon(null);
        blueLayerLabel.setIcon(null);
        alphaLayerLabel.setIcon(null);
        setSlidersEnabled(false);
        pack();
    }

    // 슬라이더 활성화/비활성화
    private void setSlidersEnabled(boolean enabled) {
        redSlider.setEnabled(enabled);
        greenSlider.setEnabled(enabled);
        blueSlider.setEnabled(enabled);
        alphaSlider.setEnabled(enabled);
    }

    // 슬라이더 값 초기화 (최대값으로)
    private void resetSliders() {
        redSlider.setValue(255);
        greenSlider.setValue(255);
        blueSlider.setValue(255);
        alphaSlider.setValue(255);
    }


    // 슬라이더 값 변경 시 레이어 업데이트
    private void updateLayers() {
        if (originalImage != null) {
            splitLayers(originalImage);
        }
    }

    private void splitLayers(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage greenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blueImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int redLevel = redSlider.getValue();
        int greenLevel = greenSlider.getValue();
        int blueLevel = blueSlider.getValue();
        int alphaLevel = alphaSlider.getValue();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                int adjustedRed = (red * redLevel) / 255;
                int adjustedGreen = (green * greenLevel) / 255;
                int adjustedBlue = (blue * blueLevel) / 255;
                int adjustedAlpha = (alpha * alphaLevel) / 255;

                adjustedRed = Math.max(0, Math.min(255, adjustedRed));
                adjustedGreen = Math.max(0, Math.min(255, adjustedGreen));
                adjustedBlue = Math.max(0, Math.min(255, adjustedBlue));
                adjustedAlpha = Math.max(0, Math.min(255, adjustedAlpha));

                redImage.setRGB(x, y, new Color(adjustedRed, 0, 0, alpha).getRGB());
                greenImage.setRGB(x, y, new Color(0, adjustedGreen, 0, alpha).getRGB());
                blueImage.setRGB(x, y, new Color(0, 0, adjustedBlue, alpha).getRGB());
                alphaImage.setRGB(x, y, new Color(adjustedAlpha, adjustedAlpha, adjustedAlpha, 255).getRGB());
            }
        }

        redLayerLabel.setIcon(new ImageIcon(redImage.getScaledInstance(LAYER_IMG_WIDTH, LAYER_IMG_HEIGHT, Image.SCALE_SMOOTH)));
        greenLayerLabel.setIcon(new ImageIcon(greenImage.getScaledInstance(LAYER_IMG_WIDTH, LAYER_IMG_HEIGHT, Image.SCALE_SMOOTH)));
        blueLayerLabel.setIcon(new ImageIcon(blueImage.getScaledInstance(LAYER_IMG_WIDTH, LAYER_IMG_HEIGHT, Image.SCALE_SMOOTH)));
        alphaLayerLabel.setIcon(new ImageIcon(alphaImage.getScaledInstance(LAYER_IMG_WIDTH, LAYER_IMG_HEIGHT, Image.SCALE_SMOOTH)));
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            new ImageLayerSplitterFrame();
        });
    }
}