package com.mygame.client.presentation.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.InputAdapter;
import com.mygame.client.presentation.navigation.Navigator;

public final class MainMenuScreen implements Screen {

    private static final int FIELD_W = 340;
    private static final int FIELD_H = 38;
    private static final int BTN_W   = 220;
    private static final int BTN_H   = 46;
    private static final int MAX_USERNAME_LEN = 20;

    private final Navigator navigator;
    private final String serverUrl;
    private String username;

    // Rendering resources (created in show())
    private ShapeRenderer shapes;
    private SpriteBatch   batch;
    private BitmapFont    titleFont;
    private BitmapFont    font;
    private GlyphLayout   layout;
    private Matrix4       proj;

    // Cursor blink state
    private float   cursorTimer  = 0f;
    private boolean showCursor   = true;

    private final InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Keys.BACKSPACE && username.length() > 0) {
                username = username.substring(0, username.length() - 1);
                return true;
            }
            if (keycode == Keys.ENTER || keycode == Keys.NUMPAD_ENTER) {
                tryConnect();
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char c) {
            // Accept printable ASCII, reject control chars and backspace (handled in keyDown)
            if (c >= 32 && c < 127 && username.length() < MAX_USERNAME_LEN) {
                username += c;
                return true;
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            int sw = Gdx.graphics.getWidth();
            int sh = Gdx.graphics.getHeight();
            int btnX = (sw - BTN_W) / 2;
            int btnY = sh / 2 - 90;
            // LibGDX touch Y is from top; convert to bottom-left origin
            int worldY = sh - screenY;
            if (screenX >= btnX && screenX <= btnX + BTN_W
                    && worldY >= btnY && worldY <= btnY + BTN_H) {
                tryConnect();
                return true;
            }
            return false;
        }
    };

    public MainMenuScreen(Navigator navigator, String serverUrl, String defaultUsername) {
        this.navigator  = navigator;
        this.serverUrl  = serverUrl;
        this.username   = defaultUsername != null ? defaultUsername : "";
    }

    // ---- Screen lifecycle ----

    @Override
    public void show() {
        shapes    = new ShapeRenderer();
        batch     = new SpriteBatch();
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.2f);
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        layout = new GlyphLayout();
        proj   = new Matrix4();
        rebuildProj();
        Gdx.input.setInputProcessor(inputAdapter);
    }

    @Override
    public void render(float delta) {
        cursorTimer += delta;
        if (cursorTimer >= 0.5f) {
            cursorTimer = 0f;
            showCursor  = !showCursor;
        }

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int fieldX = (sw - FIELD_W) / 2;
        int fieldY = sh / 2 - 10;
        int btnX   = (sw - BTN_W)  / 2;
        int btnY   = sh / 2 - 90;

        // --- Shapes ---
        shapes.setProjectionMatrix(proj);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Username field background
        shapes.setColor(0.18f, 0.18f, 0.22f, 1f);
        shapes.rect(fieldX, fieldY, FIELD_W, FIELD_H);

        // Field border highlight
        shapes.setColor(0.35f, 0.55f, 0.85f, 1f);
        shapes.rectLine(fieldX, fieldY, fieldX + FIELD_W, fieldY, 2f);
        shapes.rectLine(fieldX, fieldY + FIELD_H, fieldX + FIELD_W, fieldY + FIELD_H, 2f);
        shapes.rectLine(fieldX, fieldY, fieldX, fieldY + FIELD_H, 2f);
        shapes.rectLine(fieldX + FIELD_W, fieldY, fieldX + FIELD_W, fieldY + FIELD_H, 2f);

        // Connect button
        shapes.setColor(0.15f, 0.50f, 0.80f, 1f);
        shapes.rect(btnX, btnY, BTN_W, BTN_H);

        shapes.end();

        // --- Text ---
        batch.setProjectionMatrix(proj);
        batch.begin();

        // Title
        titleFont.setColor(new Color(1f, 0.82f, 0.15f, 1f));
        String title = "SHOOT 'EM N LOOT 'EM";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (sw - layout.width) / 2f, sh * 0.76f);

        // Field label
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Username", fieldX, fieldY + FIELD_H + 24f);

        // Field content + blinking cursor
        font.setColor(Color.WHITE);
        font.draw(batch, username + (showCursor ? "|" : " "), fieldX + 10f, fieldY + FIELD_H - 6f);

        // Button label
        font.setColor(Color.WHITE);
        String btnLabel = "CONNECT";
        layout.setText(font, btnLabel);
        font.draw(batch, btnLabel, btnX + (BTN_W - layout.width) / 2f, btnY + BTN_H - 10f);

        // Hint
        font.setColor(0.45f, 0.45f, 0.50f, 1f);
        font.getData().setScale(1.1f);
        String hint = "Press ENTER or click CONNECT";
        layout.setText(font, hint);
        font.draw(batch, hint, (sw - layout.width) / 2f, btnY - 18f);
        font.getData().setScale(1.5f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        rebuildProj();
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (shapes    != null) shapes.dispose();
        if (batch     != null) batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (font      != null) font.dispose();
    }

    // ---- Helpers ----

    private void rebuildProj() {
        if (proj == null) proj = new Matrix4();
        proj.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void tryConnect() {
        String name = username.trim();
        if (!name.isEmpty()) {
            navigator.showGame(serverUrl, name);
        }
    }
}