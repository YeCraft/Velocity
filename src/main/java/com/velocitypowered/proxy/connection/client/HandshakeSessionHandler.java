package com.velocitypowered.proxy.connection.client;

import com.google.common.base.Preconditions;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolConstants;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packets.Disconnect;
import com.velocitypowered.proxy.protocol.packets.Handshake;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import net.kyori.text.TextComponent;

public class HandshakeSessionHandler implements MinecraftSessionHandler {
    private final MinecraftConnection connection;

    public HandshakeSessionHandler(MinecraftConnection connection) {
        this.connection = Preconditions.checkNotNull(connection, "connection");
    }

    @Override
    public void handle(MinecraftPacket packet) {
        if (!(packet instanceof Handshake)) {
            throw new IllegalArgumentException("Did not expect packet " + packet.getClass().getName());
        }

        Handshake handshake = (Handshake) packet;
        switch (handshake.getNextStatus()) {
            case 1:
                // Status protocol
                connection.setState(StateRegistry.STATUS);
                connection.setProtocolVersion(handshake.getProtocolVersion());
                connection.setSessionHandler(new StatusSessionHandler(connection));
                break;
            case 2:
                connection.setState(StateRegistry.LOGIN);
                connection.setProtocolVersion(handshake.getProtocolVersion());
                if (!ProtocolConstants.isSupported(handshake.getProtocolVersion())) {
                    connection.closeWith(Disconnect.create(TextComponent.of("Unsupported client")));
                    return;
                } else {
                    connection.setSessionHandler(new LoginSessionHandler(connection));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid state " + handshake.getNextStatus());
        }

    }
}