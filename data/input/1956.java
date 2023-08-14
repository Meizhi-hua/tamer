public class test {
    private void exerciseAlgorithm(SRP srp) throws IOException {
        File f = new File(pFile);
        if (!f.exists()) {
            if (f.createNewFile()) f.deleteOnExit();
        } else if (!f.isFile()) throw new RuntimeException("File object (./test) exists but is not a file"); else if (!f.canRead() || !f.canWrite()) throw new RuntimeException("File (./test) exists but is not accessible");
        tpasswd = new PasswordFile(pFile, p2File, cFile);
        if (!tpasswd.contains(user)) {
            byte[] testSalt = new byte[10];
            prng.nextBytes(testSalt);
            tpasswd.add(user, password, testSalt, "1");
        } else tpasswd.changePasswd(user, password);
        String[] entry = tpasswd.lookup(user, srp.getAlgorithm());
        BigInteger v = new BigInteger(1, SaslUtil.fromb64(entry[0]));
        byte[] s = SaslUtil.fromb64(entry[1]);
        String[] mpi = tpasswd.lookupConfig(entry[2]);
        BigInteger N = new BigInteger(1, SaslUtil.fromb64(mpi[0]));
        BigInteger g = new BigInteger(1, SaslUtil.fromb64(mpi[1]));
        KeyPair serverKP = srp.generateServerKeyPair(N, g, v);
        KeyPair clientKP = srp.generateClientKeyPair(N, g);
        SecretKey K1 = srp.generateServerSecretKey(serverKP, ((SRPPublicKey) clientKP.getPublic()).getExponent(), v);
        MessageDigest sha = srp.newDigest();
        sha.update(s);
        sha.update(srp.userHash(user, password));
        BigInteger x = new BigInteger(1, sha.digest());
        SecretKey K2 = srp.generateClientSecretKey(clientKP, ((SRPPublicKey) serverKP.getPublic()).getExponent(), x);
        assertTrue(SaslUtil.areEqual(K1.getEncoded(), K2.getEncoded()));
        String L = "integrity=hmac-md5,replay detection";
        String o = "integrity=hmac-md5,replay detection";
        MessageDigest ckhash = srp.newDigest();
        ckhash.update(srp.xor(srp.digest(SaslUtil.trim(N)), srp.digest(SaslUtil.trim(g))));
        ckhash.update(srp.digest(user));
        ckhash.update(s);
        ckhash.update(((SRPPublicKey) clientKP.getPublic()).getExponentBytes());
        ckhash.update(((SRPPublicKey) serverKP.getPublic()).getExponentBytes());
        ckhash.update(K1.getEncoded());
        ckhash.update(srp.digest(L));
        byte[] serverExpects = ckhash.digest();
        MessageDigest hash = srp.newDigest();
        hash.update(((SRPPublicKey) clientKP.getPublic()).getExponentBytes());
        hash.update(serverExpects);
        hash.update(K1.getEncoded());
        hash.update(srp.digest(user));
        hash.update(srp.digest(user));
        hash.update(srp.digest(o));
        byte[] serverResult = hash.digest();
        hash = srp.newDigest();
        hash.update(srp.xor(srp.digest(SaslUtil.trim(N)), srp.digest(SaslUtil.trim(g))));
        hash.update(srp.digest(user));
        hash.update(s);
        hash.update(((SRPPublicKey) clientKP.getPublic()).getExponentBytes());
        hash.update(((SRPPublicKey) serverKP.getPublic()).getExponentBytes());
        hash.update(K2.getEncoded());
        hash.update(srp.digest(L));
        byte[] clientResult = hash.digest();
        ckhash = srp.newDigest();
        ckhash.update(((SRPPublicKey) clientKP.getPublic()).getExponentBytes());
        ckhash.update(clientResult);
        ckhash.update(K2.getEncoded());
        ckhash.update(srp.digest(user));
        ckhash.update(srp.digest(user));
        ckhash.update(srp.digest(o));
        byte[] clientExpects = ckhash.digest();
        assertTrue(SaslUtil.areEqual(clientResult, serverExpects));
        assertTrue(SaslUtil.areEqual(serverResult, clientExpects));
    }
}