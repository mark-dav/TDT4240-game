package com.mygame.server.domain.model;

import com.mygame.shared.dto.PlayerDto;
import com.mygame.shared.dto.WeaponType;
import com.mygame.shared.util.Vec2;

public final class PlayerState {
    public final String playerId;
    public final String username;

    public Vec2 pos;
    public Vec2 vel = Vec2.zero();
    public Vec2 facing = new Vec2(1f, 0f);

    public static final float BASE_MOVE_SPEED = 4.5f;

    public float hp = 100f;
    public float moveSpeed = BASE_MOVE_SPEED;

    public float radius = 0.30f;
    public float shootCooldownSeconds = 0f;

    public WeaponType[] inventory   = new WeaponType[2]; // slot 0 = primary, slot 1 = secondary
    public int[]        ammoBySlot  = new int[2];
    public int          currentSlot = 0;

    // kept in sync with inventory[currentSlot]
    public WeaponType equippedWeaponType = WeaponType.CROSSBOW;
    public int        equippedAmmo       = 25;

    public int lastSwitchSeq = 0;

    public int     score         = 0;
    public float   timeSurvived  = 0f;
    public boolean isDead        = false;
    public boolean justDied      = false; // true for one tick after death, triggers loot drop
    public float   respawnTimer  = 0f;
    public float   speedBoostTimer = 0f;

    public PlayerState(String playerId, String username, Vec2 spawnPos) {
        this.playerId = playerId;
        this.username = username;
        this.pos      = spawnPos;
        // Slot 0 starts with CROSSBOW; slot 1 is empty
        inventory[0]  = WeaponType.CROSSBOW;
        ammoBySlot[0] = 25; // will be overwritten by WeaponRegistry value on join
        inventory[1]  = null;
        ammoBySlot[1] = 0;
    }

    public void syncEquipped() {
        equippedWeaponType = inventory[currentSlot];
        equippedAmmo       = ammoBySlot[currentSlot];
    }

    public PlayerDto toDto() {
        PlayerDto dto = new PlayerDto(
                playerId, username, pos, vel, facing,
                hp, moveSpeed, equippedWeaponType, equippedAmmo);
        dto.score          = score;
        dto.timeSurvived   = timeSurvived;
        dto.isDead         = isDead;
        dto.respawnTimer   = respawnTimer;
        dto.speedBoostTimer = speedBoostTimer;
        // Secondary slot
        int sec = 1 - currentSlot;
        dto.secondaryWeaponType = inventory[sec];
        dto.secondaryAmmo       = ammoBySlot[sec];
        return dto;
    }
}
