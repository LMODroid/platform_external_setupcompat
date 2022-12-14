/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.setupcompat.bts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import androidx.annotation.NonNull;
import com.google.android.setupcompat.internal.Preconditions;
import com.google.android.setupcompat.util.Logger;
import java.util.concurrent.Executor;

/** Class to receive broadcast intent from SUW, and execute the client's task in the executor. */
public abstract class AbstractSetupBtsReceiver extends BroadcastReceiver {
  private static final Logger LOG = new Logger(AbstractSetupBtsReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null && getIntentAction().equals(intent.getAction())) {
      Executor executor = getExecutor();
      String simpleClassName = this.getClass().getSimpleName();
      if (executor != null) {
        executor.execute(
            () -> {
              Preconditions.ensureNotOnMainThread(simpleClassName + "::onStartTask");
              onStartTask();
            });
      }
    } else {
      LOG.w(
          "["
              + this.getClass().getSimpleName()
              + "] Unauthorized binder uid="
              + Binder.getCallingUid()
              + ", intentAction="
              + (intent == null ? "(null)" : intent.getAction()));
    }
  }

  /**
   * Gets the intent action that expected to execute the task. Use to avoid the receiver launch
   * unexpectedly.
   */
  @NonNull
  protected abstract String getIntentAction();

  /** Returns the executor used to execute the task. */
  @NonNull
  protected abstract Executor getExecutor();

  /** Tasks can be done before activity launched, in order to remove the loading before activity. */
  protected abstract void onStartTask();
}
