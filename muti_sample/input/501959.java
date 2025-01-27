public class GroupMessagingListener extends MessagingListener {
    private ConcurrentHashMap<MessagingListener, Object> mListenersMap =
        new ConcurrentHashMap<MessagingListener, Object>();
    private Set<MessagingListener> mListeners = mListenersMap.keySet();
    synchronized public void addListener(MessagingListener listener) {
        mListenersMap.put(listener, this);
    }
    synchronized public void removeListener(MessagingListener listener) {
        mListenersMap.remove(listener);
    }
    synchronized public boolean isActiveListener(MessagingListener listener) {
        return mListenersMap.containsKey(listener);
    }
    @Override
    synchronized public void listFoldersStarted(long accountId) {
        for (MessagingListener l : mListeners) {
            l.listFoldersStarted(accountId);
        }
    }
    @Override
    synchronized public void listFoldersFailed(long accountId, String message) {
        for (MessagingListener l : mListeners) {
            l.listFoldersFailed(accountId, message);
        }
    }
    @Override
    synchronized public void listFoldersFinished(long accountId) {
        for (MessagingListener l : mListeners) {
            l.listFoldersFinished(accountId);
        }
    }
    @Override
    synchronized public void synchronizeMailboxStarted(long accountId, long mailboxId) {
        for (MessagingListener l : mListeners) {
            l.synchronizeMailboxStarted(accountId, mailboxId);
        }
    }
    @Override
    synchronized public void synchronizeMailboxFinished(long accountId, long mailboxId,
            int totalMessagesInMailbox, int numNewMessages) {
        for (MessagingListener l : mListeners) {
            l.synchronizeMailboxFinished(accountId, mailboxId,
                    totalMessagesInMailbox, numNewMessages);
        }
    }
    @Override
    synchronized public void synchronizeMailboxFailed(long accountId, long mailboxId, Exception e) {
        for (MessagingListener l : mListeners) {
            l.synchronizeMailboxFailed(accountId, mailboxId, e);
        }
    }
    @Override
    synchronized public void loadMessageForViewStarted(long messageId) {
        for (MessagingListener l : mListeners) {
            l.loadMessageForViewStarted(messageId);
        }
    }
    @Override
    synchronized public void loadMessageForViewFinished(long messageId) {
        for (MessagingListener l : mListeners) {
            l.loadMessageForViewFinished(messageId);
        }
    }
    @Override
    synchronized public void loadMessageForViewFailed(long messageId, String message) {
        for (MessagingListener l : mListeners) {
            l.loadMessageForViewFailed(messageId, message);
        }
    }
    @Override
    synchronized public void checkMailStarted(Context context, long accountId, long tag) {
        for (MessagingListener l : mListeners) {
            l.checkMailStarted(context, accountId, tag);
        }
    }
    @Override
    synchronized public void checkMailFinished(Context context, long accountId, long folderId,
            long tag) {
        for (MessagingListener l : mListeners) {
            l.checkMailFinished(context, accountId, folderId, tag);
        }
    }
    @Override
    synchronized public void sendPendingMessagesStarted(long accountId, long messageId) {
        for (MessagingListener l : mListeners) {
            l.sendPendingMessagesStarted(accountId, messageId);
        }
    }
    @Override
    synchronized public void sendPendingMessagesCompleted(long accountId) {
        for (MessagingListener l : mListeners) {
            l.sendPendingMessagesCompleted(accountId);
        }
    }
    @Override
    synchronized public void sendPendingMessagesFailed(long accountId, long messageId,
            Exception reason) {
        for (MessagingListener l : mListeners) {
            l.sendPendingMessagesFailed(accountId, messageId, reason);
        }
    }
    @Override
    synchronized public void messageUidChanged(long accountId, long mailboxId,
            String oldUid, String newUid) {
        for (MessagingListener l : mListeners) {
            l.messageUidChanged(accountId, mailboxId, oldUid, newUid);
        }
    }
    @Override
    synchronized public void loadAttachmentStarted(
            long accountId,
            long messageId,
            long attachmentId,
            boolean requiresDownload) {
        for (MessagingListener l : mListeners) {
            l.loadAttachmentStarted(accountId, messageId, attachmentId, requiresDownload);
        }
    }
    @Override
    synchronized public void loadAttachmentFinished(
            long accountId,
            long messageId,
            long attachmentId) {
        for (MessagingListener l : mListeners) {
            l.loadAttachmentFinished(accountId, messageId, attachmentId);
        }
    }
    @Override
    synchronized public void loadAttachmentFailed(
            long accountId,
            long messageId,
            long attachmentId,
            String reason) {
        for (MessagingListener l : mListeners) {
            l.loadAttachmentFailed(accountId, messageId, attachmentId, reason);
        }
    }
    @Override
    synchronized public void controllerCommandCompleted(boolean moreCommandsToRun) {
        for (MessagingListener l : mListeners) {
            l.controllerCommandCompleted(moreCommandsToRun);
        }
    }
}
