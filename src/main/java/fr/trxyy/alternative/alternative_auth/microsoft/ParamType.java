package fr.trxyy.alternative.alternative_auth.microsoft;

public enum ParamType
{
    AUTH("Authentication"),
    XBL("XboxLive"),
    XSTS("XSts"),
    MC("Minecraft");

    private final String contentType;

    ParamType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getContentType()
    {
        return this.contentType;
    }
}