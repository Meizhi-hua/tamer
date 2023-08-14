public class RequiredModelMBeanMethodTest {
    public static void main(String[] args) throws Exception {
        boolean ok = true;
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        Descriptor tralalaDescriptor =
            new DescriptorSupport(new String[] {
                "name=tralala",
                "descriptorType=operation",
                "role=operation",
                "targetType=ObjectReference",
            });
        Method tralalaMethod =
            Resource.class.getMethod("tralala",
                                     new Class[] {int.class, Resource.class});
        ModelMBeanOperationInfo tralalaInfo =
            new ModelMBeanOperationInfo("tralala descr", tralalaMethod,
                                        tralalaDescriptor);
        Method remACNLMethod =
            RequiredModelMBean.class.getMethod("removeAttributeChangeNotificationListener",
                                               new Class[] {
                                                   NotificationListener.class,
                                                   String.class
                                               });
        ModelMBeanOperationInfo remACNLInfo =
            new ModelMBeanOperationInfo("remACNL descr", remACNLMethod);
        Descriptor loadDescriptor =
            new DescriptorSupport(new String[] {
                "name=load",
                "descriptorType=operation",
                "role=operation",
                "targetType=ObjectReference",
                "class=" + Resource.class.getName(),
            });
        ModelMBeanOperationInfo loadInfo =
            new ModelMBeanOperationInfo("load", "load descr",
                                        new MBeanParameterInfo[0],
                                        "void", ModelMBeanOperationInfo.ACTION,
                                        loadDescriptor);
        Descriptor storeDescriptor =
            new DescriptorSupport(new String[] {
                "name=store",
                "descriptorType=operation",
                "role=operation",
                "targetType=ObjectReference",
            });
        storeDescriptor.setField("targetObject", resource);
        ModelMBeanOperationInfo storeInfo =
            new ModelMBeanOperationInfo("store", "store descr",
                                        new MBeanParameterInfo[0],
                                        "void", ModelMBeanOperationInfo.ACTION,
                                        storeDescriptor);
        ModelMBeanInfo emptyMMBI =
            new ModelMBeanInfoSupport(Resource.class.getName(),
                                      "empty descr",
                                      null, null, null, null);
        ModelMBean emptyMMB = new RequiredModelMBean(emptyMMBI);
        emptyMMB.setManagedResource(resource, "ObjectReference");
        ObjectName emptyMMBName = new ObjectName("test:type=Empty");
        mbs.registerMBean(emptyMMB, emptyMMBName);
        System.out.println("Testing that we cannot call methods not in the " +
                           "ModelMBeanInfo");
        try {
            boolean thisok = test(mbs, emptyMMBName, false);
            if (thisok)
                System.out.println("...OK");
            else
                ok = false;
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        ModelMBeanOperationInfo[] opInfos = {
            tralalaInfo, remACNLInfo, loadInfo, storeInfo,
        };
        ModelMBeanInfo fullMMBI =
            new ModelMBeanInfoSupport(Resource.class.getName(),
                                      "full descr",
                                      null, null, opInfos, null);
        ModelMBean fullMMB = new RequiredModelMBean(fullMMBI);
        fullMMB.setManagedResource(resource, "ObjectReference");
        ObjectName fullMMBName = new ObjectName("test:type=Full");
        mbs.registerMBean(fullMMB, fullMMBName);
        System.out.println();
        System.out.println("Testing that we can call methods in the " +
                           "ModelMBeanInfo");
        System.out.println("  and that \"class\" or \"targetObject\" in " +
                           "descriptor directs methods to resource");
        try {
            boolean thisok = test(mbs, fullMMBName, true);
            if (thisok)
                System.out.println("...OK");
            else
                ok = false;
        } catch (Exception e) {
            System.out.println("TEST FAILED: Caught exception:");
            e.printStackTrace(System.out);
            ok = false;
        }
        if (ok) {
            if (!resource.loadCalled || !resource.storeCalled) {
                System.out.println("TEST FAILED: not called:" +
                                   (resource.loadCalled ? "" : " load") +
                                   (resource.storeCalled ? "" : " store"));
                ok = false;
            }
        }
        if (ok) {
            System.out.println("Testing invoke(\"class.method\")");
            resource.loadCalled = false;
            mbs.invoke(fullMMBName, Resource.class.getName() + ".load",
                       null, null);
            if (!resource.loadCalled) {
                System.out.println("TEST FAILED: load not called");
                ok = false;
            }
            try {
                mbs.invoke(fullMMBName,
                           RequiredModelMBean.class.getName() +
                           ".removeAttributeChangeNotificationListener",
                           new Object[] {boringListener, null},
                           new String[] {
                                   NotificationListener.class.getName(),
                                   String.class.getName(),
                           });
                System.out.println("TEST FAILED: removeNotificationListener" +
                                   " returned successfully but " +
                                   "should not have");
                        ok = false;
            } catch (MBeanException e) {
                final Exception target = e.getTargetException();
                if (target instanceof ListenerNotFoundException) {
                } else
                    throw e;
            }
        }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(MBeanServer mbs, ObjectName name,
                                boolean shouldWork)
            throws Exception {
        boolean ok = true;
        final String[] names = {
            "tralala",
            "removeAttributeChangeNotificationListener",
            "load",
            "store",
        };
        for (int i = 0; i < 4; i++) {
            boolean thisok = true;
            try {
                switch (i) {
                case 0:
                    String tralala = (String)
                        mbs.invoke(name, names[i],
                                   new Object[] {new Integer(5), resource},
                                   new String[] {"int",
                                                 Resource.class.getName()});
                    if (!"tralala".equals(tralala)) {
                        System.out.println("TEST FAILED: tralala returned: " +
                                           tralala);
                        thisok = false;
                    }
                    break;
                case 1:
                    try {
                        mbs.invoke(name,
                                   names[i],
                                   new Object[] {boringListener, null},
                                   new String[] {
                                       NotificationListener.class.getName(),
                                       String.class.getName(),
                                   });
                        System.out.println("TEST FAILED: " + names[i] +
                                           " returned successfully but " +
                                           "should not have");
                        thisok = false;
                    } catch (MBeanException e) {
                        final Exception target = e.getTargetException();
                        if (target instanceof ListenerNotFoundException) {
                        } else
                            throw e;
                    }
                    break;
                case 2:
                case 3:
                    mbs.invoke(name,
                               names[i],
                               new Object[0],
                               new String[0]);
                    break;
                default:
                    throw new AssertionError();
                }
                thisok = shouldWork;
                if (!shouldWork) {
                    System.out.println("TEST FAILED: " + names[i] +
                                       " worked but should not");
                }
            } catch (MBeanException e) {
                if (shouldWork) {
                    System.out.println("TEST FAILED: " + names[i] + ": " + e);
                    e.printStackTrace(System.out);
                    thisok = false;
                } else {
                    Exception target = e.getTargetException();
                    if (!(target instanceof ServiceNotFoundException)) {
                        System.out.println("TEST FAILED: " + names[i] +
                                           ": wrong exception: " + target);
                        thisok = false;
                    }
                }
            } catch (Exception e) {
                System.out.println("TEST FAILED: " + names[i] + ": " + e);
                e.printStackTrace(System.out);
                thisok = false;
            }
            if (thisok)
                System.out.println("OK: " + names[i]);
            else
                ok = false;
        }
        return ok;
    }
    public static class Resource {
        public String tralala(int x, Resource y) {
            if (x != 5 || y != this)
                return "wrong params: " + x + " " + y;
            return "tralala";
        }
        public void load() {
            loadCalled = true;
        }
        public void store() {
            storeCalled = true;
        }
        boolean loadCalled, storeCalled;
    }
    private static Resource resource = new Resource();
    private static NotificationListener boringListener =
        new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
            }
        };
}
