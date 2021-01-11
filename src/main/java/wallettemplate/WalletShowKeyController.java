/*
 * Copyright 2017 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wallettemplate;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;
import static wallettemplate.utils.GuiUtils.checkGuiThread;

/**
 *
 * @author nghiatc
 * @since Jun 28, 2017
 */
public class WalletShowKeyController {
    private static final Logger log = LoggerFactory.getLogger(WalletShowKeyController.class);

    @FXML
    ImageView imgQR;
//    @FXML Button exchangeButton;
    private static final int TAM_QRCODE = 300;

    public Main.OverlayUI overlayUI;
    private KeyParameter aesKey;
    
    // Note: NOT called by FXMLLoader!
    public void initialize(@Nullable KeyParameter aesKey) {
        if (aesKey == null) {
            if(Main.bitcoin.wallet().isEncrypted()){
                log.info("Wallet is encrypted, requesting password first.");
                // Delay execution of this until after we've finished initialising this screen.
                Platform.runLater(() -> askForPasswordAndRetry());
                return;
            } else{
                initQRCode();
            }
        } else {
            this.aesKey = aesKey;
            initQRCode();
        }
    }
    
    public void initQRCode(){
        try {
            NAuth na = NAuth.getInstance();
            String otpAuthURL = na.getOtpAuthURL();
            System.out.println("initQRCode otpAuthURL: " + otpAuthURL);
            // Encode String otpAuthURL to String Base64. Increament Security.
            // byte[] fullBytes = otpAuthURL.getBytes("UTF-8");
            // String fullString = DatatypeConverter.printBase64Binary(fullBytes);
            // System.out.println("fullString: " + fullString);

            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix matrix = writer.encode(otpAuthURL, BarcodeFormat.QR_CODE, TAM_QRCODE, TAM_QRCODE, hints);

            BufferedImage image = new BufferedImage(TAM_QRCODE, TAM_QRCODE, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, TAM_QRCODE, TAM_QRCODE);
            graphics.setColor(Color.BLACK);

            for (int i = matrix.getTopLeftOnBit()[0]; i < TAM_QRCODE; i++) {
                for (int j = matrix.getTopLeftOnBit()[1]; j < TAM_QRCODE; j++) {
                    if (matrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);

            imgQR.setImage(SwingFXUtils.toFXImage(image, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void closeClicked(ActionEvent event) {
        overlayUI.done();
    }
    
    private void askForPasswordAndRetry() {
        Main.OverlayUI<WalletPasswordController> pwd = Main.instance.overlayUI("wallet_password.fxml");
        pwd.controller.aesKeyProperty().addListener((observable, old, cur) -> {
            // We only get here if the user found the right password. If they don't or they cancel, we end up back on
            // the main UI screen.
            checkGuiThread();
            Main.OverlayUI<WalletShowKeyController> screen = Main.instance.overlayUI("wallet_show_key.fxml");
            screen.controller.initialize(cur);
        });
    }
    
}














