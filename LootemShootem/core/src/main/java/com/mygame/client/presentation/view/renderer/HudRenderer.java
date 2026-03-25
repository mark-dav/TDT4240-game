package com.mygame.client.presentation.view.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.mygame.client.domain.model.WorldState;
import com.mygame.client.presentation.view.input.InputHandler;
import com.mygame.shared.dto.GameSnapshotDto;
import com.mygame.shared.dto.PlayerDto;

import java.util.ArrayDeque;
import java.util.Iterator;

public final class HudRenderer {

    private static final float KILL_FEED_TTL = 4f;

    private static final class FeedEntry {
        String text;
        float  ttl;
        FeedEntry(String text) { this.text = text; this.ttl = KILL_FEED_TTL; }
    }

    private final WorldState   worldState;
    private final InputHandler inputHandler;
    private final ShapeRenderer shapes;
    private final SpriteBatch  batch;
    private final BitmapFont   font;
    private final BitmapFont   bigFont;
    private final GlyphLayout  layout = new GlyphLayout();
    private       Matrix4      screenProj;

    private final ArrayDeque<FeedEntry> killFeed = new ArrayDeque<>();
    private long lastProcessedTick = -1;

    public HudRenderer(WorldState worldState,
                       InputHandler inputHandler,
                       ShapeRenderer shapes,
                       SpriteBatch batch,
                       BitmapFont font,
                       BitmapFont bigFont) {
        this.worldState   = worldState;
        this.inputHandler = inputHandler;
        this.shapes       = shapes;
        this.batch        = batch;
        this.font         = font;
        this.bigFont      = bigFont;
        rebuildScreenProj();
    }

    public void resize() {
        rebuildScreenProj();
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        GameSnapshotDto snap = worldState.getSnapshot();
        PlayerDto       me   = worldState.getLocalPlayer();

        // Ingest new kill feed entries from the latest snapshot (once per tick)
        if (snap != null && snap.tick > lastProcessedTick) {
            lastProcessedTick = snap.tick;
            if (snap.killFeed != null) {
                for (String msg : snap.killFeed) {
                    if (msg != null && !msg.isEmpty()) killFeed.addLast(new FeedEntry(msg));
                }
            }
        }

        // Age existing entries
        Iterator<FeedEntry> it = killFeed.iterator();
        while (it.hasNext()) {
            FeedEntry e = it.next();
            e.ttl -= delta;
            if (e.ttl <= 0f) it.remove();
        }

        if (snap == null || me == null) {
            drawConnecting();
            return;
        }

        if (me.isDead) {
            drawDeathOverlay(me);
        } else {
            drawHealthBar(me);
            drawPlayerInfo(me, snap);
        }

        drawKillFeed();

        if (inputHandler.isAndroid()) drawTouchControls();
    }

    private void drawHealthBar(PlayerDto me) {
        float hpFrac = Math.max(0f, Math.min(1f, me.hp / 100f));
        int barX = 20, barY = 20, barW = 200, barH = 18;

        shapes.setProjectionMatrix(screenProj);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.25f, 0.25f, 0.25f, 1f);
        shapes.rect(barX, barY, barW, barH);
        shapes.setColor(
                hpFrac < 0.5f ? 0.9f : (2f - 2f * hpFrac) * 0.9f,
                hpFrac > 0.5f ? 0.85f : (2f * hpFrac) * 0.85f,
                0.10f, 1f);
        shapes.rect(barX, barY, barW * hpFrac, barH);
        shapes.end();
    }

    private void drawPlayerInfo(PlayerDto me, GameSnapshotDto snap) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();
        int barY = 20, barH = 18;

        batch.setProjectionMatrix(screenProj);
        batch.begin();

        // HP label
        font.setColor(Color.WHITE);
        font.draw(batch, "HP  " + (int) me.hp + " / 100", 20, barY + barH + 18f);

        // Primary weapon
        String primary = me.equippedWeaponType != null
                ? "[" + me.equippedWeaponType.name() + "]  " + me.equippedAmmo + " ammo" : "—";
        font.setColor(new Color(0.95f, 0.95f, 0.60f, 1f));
        font.draw(batch, primary, 20, barY + barH + 36f);

        // Secondary weapon (FR7)
        if (me.secondaryWeaponType != null) {
            font.setColor(new Color(0.70f, 0.70f, 0.70f, 1f));
            String secondary = me.secondaryWeaponType.name() + "  " + me.secondaryAmmo + " ammo  [SPACE]";
            font.draw(batch, secondary, 20, barY + barH + 56f);
        }

        // Speed boost
        if (me.speedBoostTimer > 0f) {
            font.setColor(new Color(0.20f, 0.85f, 0.95f, 1f));
            font.draw(batch, "SPEED BOOST  " + (int) Math.ceil(me.speedBoostTimer) + "s",
                    20, barY + barH + (me.secondaryWeaponType != null ? 76f : 56f));
        }

        // Kill score + time survived (top-right)
        font.setColor(new Color(1f, 0.85f, 0.2f, 1f));
        String scoreText = "Kills: " + me.score;
        layout.setText(font, scoreText);
        font.draw(batch, scoreText, sw - layout.width - 20f, sh - 20f);

        font.setColor(Color.LIGHT_GRAY);
        int mins = (int) me.timeSurvived / 60;
        int secs = (int) me.timeSurvived % 60;
        String timeText = String.format("Time: %d:%02d", mins, secs);
        layout.setText(font, timeText);
        font.draw(batch, timeText, sw - layout.width - 20f, sh - 42f);

        // Leaderboard
        float lbY = sh - 70f;
        String localId = worldState.getLocalPlayerId();
        for (PlayerDto p : snap.players) {
            if (localId != null && localId.equals(p.playerId)) continue;
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, p.username + ": " + p.score + " kills", sw - 200f, lbY);
            lbY -= 22f;
        }

        batch.end();
    }

    private void drawKillFeed() {
        if (killFeed.isEmpty()) return;
        int sh = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(screenProj);
        batch.begin();

        float feedY = sh * 0.55f;
        for (FeedEntry e : killFeed) {
            float alpha = Math.min(1f, e.ttl); // fade out in last second
            font.setColor(1f, 0.85f, 0.40f, alpha);
            font.draw(batch, e.text, 20f, feedY);
            feedY += 22f;
        }

        batch.end();
    }

    private void drawDeathOverlay(PlayerDto me) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(screenProj);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0, 0, sw, sh);
        shapes.end();

        batch.setProjectionMatrix(screenProj);
        batch.begin();

        bigFont.setColor(new Color(0.9f, 0.2f, 0.2f, 1f));
        layout.setText(bigFont, "YOU DIED");
        bigFont.draw(batch, "YOU DIED", (sw - layout.width) / 2f, sh / 2f + 60f);

        font.setColor(Color.WHITE);
        String respawn = "Respawning in " + Math.max(1, (int) Math.ceil(me.respawnTimer)) + "...";
        layout.setText(font, respawn);
        font.draw(batch, respawn, (sw - layout.width) / 2f, sh / 2f - 10f);

        batch.end();

        drawKillFeed();
    }

    private void drawConnecting() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();
        batch.setProjectionMatrix(screenProj);
        batch.begin();
        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "Connecting...");
        font.draw(batch, "Connecting...", (sw - layout.width) / 2f, sh / 2f);
        batch.end();
    }

    private void drawTouchControls() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(screenProj);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        inputHandler.moveStick.render(shapes);
        inputHandler.aimStick.render(shapes);
        shapes.setColor(0.55f, 0.30f, 0.80f, 0.75f);
        shapes.circle(inputHandler.switchBtnX, inputHandler.switchBtnY, inputHandler.switchBtnR, 24);
        shapes.setColor(0.35f, 0.15f, 0.55f, 0.85f);
        shapes.circle(inputHandler.switchBtnX, inputHandler.switchBtnY, inputHandler.switchBtnR - 5f, 24);
        shapes.end();

        batch.setProjectionMatrix(screenProj);
        batch.begin();
        font.setColor(Color.WHITE);
        layout.setText(font, "SW");
        font.draw(batch, "SW",
                inputHandler.switchBtnX - layout.width / 2f,
                inputHandler.switchBtnY + layout.height / 2f);
        batch.end();
    }

    private void rebuildScreenProj() {
        if (screenProj == null) screenProj = new Matrix4();
        screenProj.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
