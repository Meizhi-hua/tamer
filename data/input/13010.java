public class BadFooter {
    public static void main(String[] args) throws Exception {
        String cert =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDGDCCAtWgAwIBAgIERgH/AjALBgcqhkjOOAQDBQAwXTELMAkGA1UEBhMCRUExDTALBgNVBAgT\n" +
        "BE1vb24xETAPBgNVBAcTCEJhY2tzaWRlMQ4wDAYDVQQKEwVBLUItQzEPMA0GA1UECxMGT2ZmaWNl\n" +
        "MQswCQYDVQQDEwJNZTAeFw0wNzAzMjIwMzU4NThaFw0wNzA2MjAwMzU4NThaMF0xCzAJBgNVBAYT\n" +
        "AkVBMQ0wCwYDVQQIEwRNb29uMREwDwYDVQQHEwhCYWNrc2lkZTEOMAwGA1UEChMFQS1CLUMxDzAN\n" +
        "BgNVBAsTBk9mZmljZTELMAkGA1UEAxMCTWUwggG4MIIBLAYHKoZIzjgEATCCAR8CgYEA/X9TgR11\n" +
        "EilS30qcLuzk5/YRt1I870QAwx4/gLZRJmlFXUAiUftZPY1Y+r/F9bow9subVWzXgTuAHTRv8mZg\n" +
        "t2uZUKWkn5/oBHsQIsJPu6nX/rfGG/g7V+fGqKYVDwT7g/bTxR7DAjVUE1oWkTL2dfOuK2HXKu/y\n" +
        "IgMZndFIAccCFQCXYFCPFSMLzLKSuYKi64QL8Fgc9QKBgQD34aCF1ps93su8q1w2uFe5eZSvu/o6\n" +
        "6oL5V0wLPQeCZ1FZV4661FlP5nEHEIGAtEkWcSPoTCgWE7fPCTKMyKbhPBZ6i1R8jSjgo64eK7Om\n" +
        "dZFuo38L+iE1YvH7YnoBJDvMpPG+qFGQiaiD3+Fa5Z8GkotmXoB7VSVkAUw7/s9JKgOBhQACgYEA\n" +
        "xc7ovvDeJ5yIkiEoz6U4jcFf5ZDSC+rUEsqGuARXHUF0PlIth7h2e9KV12cwdjVH++mGvwU/m/Ju\n" +
        "OpaaWOEFRHgCMe5fZ2xE0pWPcmKkPicc85SKHguYTMCc9D0XbTbkoBIEAeQ4nr2GmXuEQ5tYaO/O\n" +
        "PYXjk9EfGhikHlnKgC6jITAfMB0GA1UdDgQWBBTtv4rKVwXtXJpyZWlswQL4MAKkazALBgcqhkjO\n" +
        "OAQDBQADMAAwLQIVAIU4pnnUcMjh2CUvh/B0PSZZTHHvAhQVMhAdwNHOGPSL6sCL19q6UjoN9w==\n" +
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDGDCCAtWgAwIBAgIERgH/AjALBgcqhkjOOAQDBQAwXTELMAkGA1UEBhMCRUExDTALBgNVBAgT\n" +
        "BE1vb24xETAPBgNVBAcTCEJhY2tzaWRlMQ4wDAYDVQQKEwVBLUItQzEPMA0GA1UECxMGT2ZmaWNl\n" +
        "MQswCQYDVQQDEwJNZTAeFw0wNzAzMjIwMzU4NThaFw0wNzA2MjAwMzU4NThaMF0xCzAJBgNVBAYT\n" +
        "AkVBMQ0wCwYDVQQIEwRNb29uMREwDwYDVQQHEwhCYWNrc2lkZTEOMAwGA1UEChMFQS1CLUMxDzAN\n" +
        "BgNVBAsTBk9mZmljZTELMAkGA1UEAxMCTWUwggG4MIIBLAYHKoZIzjgEATCCAR8CgYEA/X9TgR11\n" +
        "EilS30qcLuzk5/YRt1I870QAwx4/gLZRJmlFXUAiUftZPY1Y+r/F9bow9subVWzXgTuAHTRv8mZg\n" +
        "t2uZUKWkn5/oBHsQIsJPu6nX/rfGG/g7V+fGqKYVDwT7g/bTxR7DAjVUE1oWkTL2dfOuK2HXKu/y\n" +
        "IgMZndFIAccCFQCXYFCPFSMLzLKSuYKi64QL8Fgc9QKBgQD34aCF1ps93su8q1w2uFe5eZSvu/o6\n" +
        "6oL5V0wLPQeCZ1FZV4661FlP5nEHEIGAtEkWcSPoTCgWE7fPCTKMyKbhPBZ6i1R8jSjgo64eK7Om\n" +
        "dZFuo38L+iE1YvH7YnoBJDvMpPG+qFGQiaiD3+Fa5Z8GkotmXoB7VSVkAUw7/s9JKgOBhQACgYEA\n" +
        "xc7ovvDeJ5yIkiEoz6U4jcFf5ZDSC+rUEsqGuARXHUF0PlIth7h2e9KV12cwdjVH++mGvwU/m/Ju\n" +
        "OpaaWOEFRHgCMe5fZ2xE0pWPcmKkPicc85SKHguYTMCc9D0XbTbkoBIEAeQ4nr2GmXuEQ5tYaO/O\n" +
        "PYXjk9EfGhikHlnKgC6jITAfMB0GA1UdDgQWBBTtv4rKVwXtXJpyZWlswQL4MAKkazALBgcqhkjO\n" +
        "OAQDBQADMAAwLQIVAIU4pnnUcMjh2CUvh/B0PSZZTHHvAhQVMhAdwNHOGPSL6sCL19q6UjoN9w==\n" +
        "-----END CERTIFICATE-----\n";
        try {
            CertificateFactory.getInstance("X509").generateCertificates(
                    new ByteArrayInputStream(cert.getBytes()));
            throw new Exception("Fail. certificate generation should fail");
        } catch (CertificateException ce) {
            ce.printStackTrace();
        }
    }
}
