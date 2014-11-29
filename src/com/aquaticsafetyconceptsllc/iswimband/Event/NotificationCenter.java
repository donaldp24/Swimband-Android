package com.aquaticsafetyconceptsllc.iswimband.Event;

import java.util.ArrayList;

/**
 * Created by donaldpae on 11/26/14.
 */
public class NotificationCenter {

    private class ObserverWrapper {
        public Object observer;
        public String name;
        public Object object;
        public INotificationHandler handler;
    }

    private static NotificationCenter _instance = null;
    private ArrayList<ObserverWrapper> observerWrappers;

    public static NotificationCenter sharedInstance() {
        if (_instance == null)
            _instance = new NotificationCenter();
        return _instance;
    }

    private NotificationCenter() {
        observerWrappers = new ArrayList<ObserverWrapper>();
    }

    public void addObserver(Object observer, String name, Object object, INotificationHandler handler) {
        ObserverWrapper exist = findObserverWrapper(observer, name);
        if (exist != null) {
            exist.object = object;
            exist.handler = handler;
        } else {
            ObserverWrapper newOne = new ObserverWrapper();
            newOne.observer = observer;
            newOne.object = object;
            newOne.handler = handler;

            observerWrappers.add(newOne);
        }
    }

    public void removeObserver(Object observer, String name) {
        ObserverWrapper exist = findObserverWrapper(observer, name);
        if (exist == null)
            return;
        observerWrappers.remove(exist);
    }

    public void postNotification(String name, Object object) {
        for (ObserverWrapper wrapper : observerWrappers) {
            if (wrapper.name.equalsIgnoreCase(name)) {
                // post
                Notification notification = new Notification();
                notification.object = wrapper.object;
                wrapper.handler.handle(wrapper.observer, notification);
            }
        }
    }

    protected ObserverWrapper findObserverWrapper(Object observer, String name) {
        for (ObserverWrapper exist : observerWrappers) {
            if (exist.observer == observer &&
                    exist.name.equalsIgnoreCase(name))
                return exist;
        }
        return null;
    }
}
