/*
 * Copyright 2022 The Android Open Source Project
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
package androidx.media3.session;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.Player;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowPendingIntent;

/** Tests for {@link DefaultActionFactory}. */
@RunWith(AndroidJUnit4.class)
public class DefaultActionFactoryTest {

  @Test
  public void createMediaPendingIntent_intentIsMediaAction() {
    DefaultActionFactory actionFactory =
        new DefaultActionFactory(Robolectric.setupService(TestService.class));
    MediaSession mockMediaSession = mock(MediaSession.class);
    MediaSessionImpl mockMediaSessionImpl = mock(MediaSessionImpl.class);
    when(mockMediaSession.getImpl()).thenReturn(mockMediaSessionImpl);
    Uri dataUri = Uri.parse("http://example.com");
    when(mockMediaSessionImpl.getUri()).thenReturn(dataUri);

    PendingIntent pendingIntent =
        actionFactory.createMediaActionPendingIntent(mockMediaSession, Player.COMMAND_PLAY_PAUSE);

    ShadowPendingIntent shadowPendingIntent = shadowOf(pendingIntent);
    assertThat(actionFactory.isMediaAction(shadowPendingIntent.getSavedIntent())).isTrue();
    assertThat(shadowPendingIntent.getSavedIntent().getData()).isEqualTo(dataUri);
  }

  @Test
  public void isMediaAction_withNonMediaIntent_returnsFalse() {
    DefaultActionFactory actionFactory =
        new DefaultActionFactory(Robolectric.setupService(TestService.class));

    Intent intent = new Intent("invalid_action");

    assertThat(actionFactory.isMediaAction(intent)).isFalse();
  }

  @Test
  public void isCustomAction_withNonCustomActionIntent_returnsFalse() {
    DefaultActionFactory actionFactory =
        new DefaultActionFactory(Robolectric.setupService(TestService.class));

    Intent intent = new Intent("invalid_action");

    assertThat(actionFactory.isCustomAction(intent)).isFalse();
  }

  @Test
  public void createCustomActionFromCustomCommandButton() {
    DefaultActionFactory actionFactory =
        new DefaultActionFactory(Robolectric.setupService(TestService.class));
    MediaSession mockMediaSession = mock(MediaSession.class);
    MediaSessionImpl mockMediaSessionImpl = mock(MediaSessionImpl.class);
    when(mockMediaSession.getImpl()).thenReturn(mockMediaSessionImpl);
    Uri dataUri = Uri.parse("http://example.com");
    when(mockMediaSessionImpl.getUri()).thenReturn(dataUri);
    Bundle commandBundle = new Bundle();
    commandBundle.putString("command-key", "command-value");
    Bundle buttonBundle = new Bundle();
    buttonBundle.putString("button-key", "button-value");
    CommandButton customSessionCommand =
        new CommandButton.Builder()
            .setSessionCommand(new SessionCommand("a", commandBundle))
            .setExtras(buttonBundle)
            .setIconResId(R.drawable.media3_notification_pause)
            .setDisplayName("name")
            .build();

    NotificationCompat.Action notificationAction =
        actionFactory.createCustomActionFromCustomCommandButton(
            mockMediaSession, customSessionCommand);

    ShadowPendingIntent shadowPendingIntent = shadowOf(notificationAction.actionIntent);
    assertThat(shadowPendingIntent.getSavedIntent().getData()).isEqualTo(dataUri);
    assertThat(String.valueOf(notificationAction.title)).isEqualTo("name");
    assertThat(notificationAction.getIconCompat().getResId())
        .isEqualTo(R.drawable.media3_notification_pause);
    assertThat(notificationAction.getExtras().size()).isEqualTo(0);
    assertThat(notificationAction.getActionIntent()).isNotNull();
  }

  @Test
  public void
      createCustomActionFromCustomCommandButton_notACustomAction_throwsIllegalArgumentException() {
    DefaultActionFactory actionFactory =
        new DefaultActionFactory(Robolectric.setupService(TestService.class));
    CommandButton customSessionCommand =
        new CommandButton.Builder()
            .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
            .setIconResId(R.drawable.media3_notification_pause)
            .setDisplayName("name")
            .build();

    Assert.assertThrows(
        IllegalArgumentException.class,
        () ->
            actionFactory.createCustomActionFromCustomCommandButton(
                mock(MediaSession.class), customSessionCommand));
  }

  /** A test service for unit tests. */
  public static final class TestService extends MediaLibraryService {
    @Nullable
    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
      return null;
    }
  }
}
