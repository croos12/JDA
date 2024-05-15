/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class ManliusPCBot extends ListenerAdapter
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Unable to start without token!");
            System.exit(1);
        }
        String token = args[0];

        // We only need 3 gateway intents enabled for this example:
        EnumSet<GatewayIntent> intents = EnumSet.of(
            // We need messages in guilds to accept commands from users
            GatewayIntent.GUILD_MESSAGES,
            // We need voice states to connect to the voice channel
            GatewayIntent.GUILD_VOICE_STATES,
            // Enable access to message.getContentRaw()
            GatewayIntent.MESSAGE_CONTENT
        );

        // Start the JDA session with default mode (voice member cache)
        JDABuilder.createDefault(token, intents)         // Use provided token from command line arguments
             .addEventListeners(new ManliusPCBot())  // Start listening with this listener
             .setActivity(Activity.competing("Certamen")) // Inform users that we are competing in Certamen
             .setStatus(OnlineStatus.ONLINE)     // Online and ready to party'
             .enableCache(CacheFlag.VOICE_STATE)         // Enable the VOICE_STATE cache to find a user's connected voice channel
             .build();                                   // Login with these options
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Message message = event.getMessage();
        User author = message.getAuthor();
        String content = message.getContentRaw();
        Guild guild = event.getGuild();

        // Ignore message if bot
        if (author.isBot())
            return;

        // We only want to handle message in Guilds
        if (!event.isFromGuild())
        {
            return;
        }

        if (content.startsWith("!repeat"))
        {
            String args = content.substring("!repeat".length());

            onRepeatCommand(event, guild, args);
        }

        if(content.startsWith("!scream")){
            onScreamCommand(event);
        }

        if(content.startsWith("!hello")){
            onHelloCommand(event);
        }
    }

    /**
     * Handle command with arguments.
     *
     * @param event
     *        The event for this command
     */
    private void onHelloCommand(MessageReceivedEvent event)
    {
        MessageChannel messageChannel = event.getChannel();

        messageChannel.sendMessage("Hello World I am Manlius PC");
    }

    /**
     * Handle command with arguments.
     *
     * @param event
     *        The event for this command
     * @param guild
     *        The guild where its happening
     * @param repeat
     *        The message Jacob will say
     */
    private void onRepeatCommand(MessageReceivedEvent event, Guild guild, String repeat)
    {
        VoiceChannel voiceChannel = null;

        MessageChannel messageChannel = event.getChannel();

        if(repeat.isEmpty()){
            messageChannel.sendMessage("!repeat command is missing message'");
        }

        messageChannel.sendMessage(repeat);
        // Generate Audio

        //messageChannel.sendMessage()
        //connectTo(voiceChannel );                     // We found a channel to connect to!
        //onConnecting(voiceChannel , messageChannel);     // Let the user know, we were successful!
    }

    /**
     * Handle command with arguments.
     *
     * @param event
     *        The event for this command
     */
    private void onScreamCommand(MessageReceivedEvent event)
    {
        MessageChannel messageChannel = event.getChannel();

        messageChannel.sendMessage("AAAAAAAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHHHHH");

        try{
            TimeUnit.SECONDS.sleep(5);
        }
        catch(InterruptedException e){
            messageChannel.sendMessage("Manlius PC shutting down");
            Thread.currentThread().interrupt();
        }

        messageChannel.sendMessage("Get me out of here!!!!!!");

        // Generate Audio

        //messageChannel.sendMessage()
        //connectTo(voiceChannel );                     // We found a channel to connect to!
        //onConnecting(voiceChannel , messageChannel);     // Let the user know, we were successful!
    }

    /**
     * Inform user about successful connection.
     *
     * @param channel
     *        The voice channel we connected to
     * @param messageChannel
     *        The text channel to send the message in
     */
    private void onConnecting(AudioChannel channel, MessageChannel messageChannel)
    {
        messageChannel.sendMessage("Connecting to " + channel.getName()).queue(); // never forget to queue()!
    }

    /**
     * The channel to connect to is not known to us.
     *
     * @param channel
     *        The message channel (text channel abstraction) to send failure information to
     * @param comment
     *        The information of this channel
     */
    private void onUnknownChannel(MessageChannel channel, String comment)
    {
        channel.sendMessage("Unable to connect to ``" + comment + "``, no such channel!").queue(); // never forget to queue()!
    }

    /**
     * Connect to requested channel and start echo handler
     *
     * @param channel
     *        The channel to connect to
     */
    private void connectTo(AudioChannel channel)
    {
        Guild guild = channel.getGuild();
        // Get an audio manager for this guild, this will be created upon first use for each guild
        AudioManager audioManager = guild.getAudioManager();
        // Create our Send/Receive handler for the audio connection
        TrappedJacobSoul trappedJacobSoul = new TrappedJacobSoul();

        // The order of the following instructions does not matter!

        // Set the sending handler to our echo system
        audioManager.setSendingHandler(trappedJacobSoul);
        // Connect to the voice channel
        audioManager.openAudioConnection(channel);
    }

    public static class TrappedJacobSoul implements AudioSendHandler
    {
        /*
            All methods in this class are called by JDA threads when resources are available/ready for processing.

            The receiver will be provided with the latest 20ms of PCM stereo audio
            Note you can receive even while setting yourself to deafened!

            The sender will provide 20ms of PCM stereo audio (pass-through) once requested by JDA
            When audio is provided JDA will automatically set the bot to speaking!
         */
        private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

        /* Send Handling */

        @Override
        public boolean canProvide()
        {
            // If we have something in our buffer we can provide it to the send system
            return !queue.isEmpty();
        }

        @Override
        public ByteBuffer provide20MsAudio()
        {
            // use what we have in our buffer to send audio as PCM
            byte[] data = queue.poll();
            return data == null ? null : ByteBuffer.wrap(data); // Wrap this in a java.nio.ByteBuffer
        }

        @Override
        public boolean isOpus()
        {
            // since we send audio that is received from discord we don't have opus but PCM
            return false;
        }
    }
}
