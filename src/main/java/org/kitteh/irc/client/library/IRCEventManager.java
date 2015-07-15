/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library;

import net.engio.mbassy.bus.SyncMessageBus;
import net.engio.mbassy.bus.common.Properties;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.exception.KittehEventException;
import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

final class IRCEventManager implements EventManager {
    private class Exceptional implements IPublicationErrorHandler {
        @Override
        public void handleError(@Nonnull PublicationError publicationError) {
            IRCEventManager.this.client.getExceptionListener().queue(new KittehEventException(publicationError.getCause()));
        }
    }

    private final SyncMessageBus<Object> bus = new SyncMessageBus<>(new BusConfiguration().addFeature(Feature.SyncPubSub.Default()).setProperty(Properties.Handler.PublicationError, new Exceptional()));
    private final InternalClient client;
    private final Set<Object> listeners = new HashSet<>();

    IRCEventManager(@Nonnull InternalClient client) {
        this.client = client;
    }

    public void callEvent(@Nonnull Object event) {
        Sanity.nullCheck(event, "Event cannot be null");
        this.bus.publish(event);
    }

    @Nonnull
    public synchronized Set<Object> getRegisteredEventListeners() {
        return new HashSet<>(this.listeners);
    }

    public synchronized void registerEventListener(@Nonnull Object listener) {
        this.listeners.add(listener);
        this.bus.subscribe(listener);
    }

    public synchronized void unregisterEventListener(@Nonnull Object listener) {
        this.listeners.remove(listener);
        this.bus.unsubscribe(listener);
    }
}