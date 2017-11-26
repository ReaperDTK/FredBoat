package fredboat.command.music.control;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fredboat.audio.player.GuildPlayer;
import fredboat.audio.player.PlayerLimitManager;
import fredboat.audio.player.PlayerRegistry;
import fredboat.commandmeta.abs.CommandContext;
import fredboat.util.TextUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.GuildVoiceState;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.TextChannel;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PlayerLimitManager.class, PlayerRegistry.class, TextUtils.class })
public class PlayCommandTest {

	PlayCommand command;
	Guild guild;
	TextChannel channel;
	Member invoker;
	Message message;
	GuildVoiceState voiceState;
	CommandContext context;
	GuildPlayer player;

	String validYoutubeURL = "https://www.youtube.com/watch?v=yBLdQ1a4-JI";

	@Before
	public void setUp() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// ************ MOCK INITIALIZATION ************
		guild = Mockito.mock(Guild.class);
		channel = Mockito.mock(TextChannel.class);
		invoker = Mockito.mock(Member.class);
		message = Mockito.mock(Message.class);
		voiceState = Mockito.mock(GuildVoiceState.class);
		player = Mockito.mock(GuildPlayer.class);

		// We use Reflection to mock final fields instances of CommandContext
		Constructor<CommandContext> contextConstructor = CommandContext.class.getDeclaredConstructor(Guild.class,
				TextChannel.class, Member.class, Message.class);
		contextConstructor.setAccessible(true);
		context = Mockito.spy(contextConstructor.newInstance(guild, channel, invoker, message));

		PowerMockito.mockStatic(PlayerLimitManager.class);
		PowerMockito.mockStatic(PlayerRegistry.class);
		PowerMockito.mockStatic(TextUtils.class);
	}

	@Test
	public void invokePlayCommand() {

		// ************ SET MOCKS CALLS AND RETURN VALUES ************
		// Insert in the context.args the url
		context.args = new String[] { validYoutubeURL };
		Mockito.when(invoker.getVoiceState()).thenReturn(voiceState);
		Mockito.when(voiceState.inVoiceChannel()).thenReturn(true);
		PowerMockito.when(PlayerLimitManager.checkLimitResponsive(context)).thenReturn(true);
		Mockito.when(message.getAttachments()).thenReturn(new ArrayList<Attachment>());
		Mockito.when(context.hasArguments()).thenReturn(true);
		PowerMockito.when(TextUtils.isSplitSelect(context.rawArgs)).thenReturn(false);
		PowerMockito.when(PlayerRegistry.getOrCreate(context.guild)).thenReturn(player);

		// ************ VERIFY ************
		PlayCommand command = new PlayCommand(null, "play");

		command.onInvoke(context);
		// check that the command queues the url
		Mockito.verify(player).queue(validYoutubeURL, context);

		Mockito.verify(player).setPause(false);

		Mockito.verify(context).deleteMessage();

	}
}
