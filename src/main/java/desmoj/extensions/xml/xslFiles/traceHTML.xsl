<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <xsl:include href="traceHTMLParam.xsl"/>
    <xsl:template match="/trace">
        <html>
            <head>
                <title>Trace of Experiment
                    <xsl:value-of select="@experiment"/>
                </title>
            </head>
            <body>
                <h2>Trace of Experiment
                    <xsl:value-of select="@experiment"/>
                </h2>
                <table>
                    <tr>
                        <th align="left">Model</th>
                        <th align="left">Modeltime</th>
                        <th align="left">Event / Entity</th>
                        <th align="left">Action</th>
                    </tr>
                    <xsl:for-each select="//note">
                        <xsl:if test="(@modeltime >= $minimum) and (@modeltime &lt;= $maximum)">
                            <xsl:if test="(entity=$entityname) or (event=$eventname) or ($entityname='' and $eventname='') or ((event) and ($eventname='')) or ((entity) and ($entityname=''))">
                                <tr valign="top">
                                    <td>
                                        <xsl:value-of select="model"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="@modeltime"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="entity"/><xsl:value-of select="event"/>
                                    </td>
                                    <td>
                                        <xsl:value-of select="actions"/>
                                    </td>
                                </tr>
                            </xsl:if>
                        </xsl:if>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>