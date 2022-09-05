<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="yes"/>
    <xsl:param name="maxReporters" select="10"/>
    <xsl:param name="maxParameters" select="15"/>
    <xsl:include href="reportHTMLParam.xsl"/>
    <xsl:template match="/report">
        <html>
            <head>
                <title>Report of model
                    <xsl:value-of select="@model"/> experiment
                    <xsl:value-of select="@experiment"/>
                </title>
            </head>
            <body>
                <h2>Report of model
                    <xsl:value-of select="@model"/> experiment
                    <xsl:value-of select="@experiment"/>
                </h2>
                <xsl:apply-templates/>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="//report/param">
        <p>
            <xsl:value-of select="."/>
        </p>
    </xsl:template>
    <xsl:template match="//reporter">
        <xsl:if test="($allReporters = 1) or (@type = $reporter1) or (@type = $reporter2) or (@type = $reporter3) or (@type = $reporter4) or (@type = $reporter5) or (@type = $reporter6) or (@type = $reporter7) or (@type = $reporter8) or (@type = $reporter9) or (@type = $reporter10)">
            <h3>
                <xsl:value-of select="@type"/>
            </h3>
            <table>
                <tr bgcolor="#cccccc">
                    <th>name</th>
                    <xsl:for-each select="item[1]/param">
                        <xsl:choose>
                            <xsl:when test="$allReporters = 1">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter1) and (($reporter1allParam = 1) or (@name = $reporter1Param1) or (@name = $reporter1Param2) or (@name = $reporter1Param3) or (@name = $reporter1Param4) or (@name = $reporter1Param5) or (@name = $reporter1Param6) or (@name = $reporter1Param7) or (@name = $reporter1Param8) or (@name = $reporter1Param9) or (@name = $reporter1Param10) or (@name = $reporter1Param11) or (@name = $reporter1Param12) or (@name = $reporter1Param13) or (@name = $reporter1Param14) or (@name = $reporter1Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter2) and (($reporter2allParam = 1) or (@name = $reporter2Param1) or (@name = $reporter2Param2) or (@name = $reporter2Param3) or (@name = $reporter2Param4) or (@name = $reporter2Param5) or (@name = $reporter2Param6) or (@name = $reporter2Param7) or (@name = $reporter2Param8) or (@name = $reporter2Param9) or (@name = $reporter2Param10) or (@name = $reporter2Param11) or (@name = $reporter2Param12) or (@name = $reporter2Param13) or (@name = $reporter2Param14) or (@name = $reporter2Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter3) and (($reporter3allParam = 1) or (@name = $reporter3Param1) or (@name = $reporter3Param2) or (@name = $reporter3Param3) or (@name = $reporter3Param4) or (@name = $reporter3Param5) or (@name = $reporter3Param6) or (@name = $reporter3Param7) or (@name = $reporter3Param8) or (@name = $reporter3Param9) or (@name = $reporter3Param10) or (@name = $reporter3Param11) or (@name = $reporter3Param12) or (@name = $reporter3Param13) or (@name = $reporter3Param14) or (@name = $reporter3Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter4) and (($reporter4allParam = 1) or (@name = $reporter4Param1) or (@name = $reporter4Param2) or (@name = $reporter4Param3) or (@name = $reporter4Param4) or (@name = $reporter4Param5) or (@name = $reporter4Param6) or (@name = $reporter4Param7) or (@name = $reporter4Param8) or (@name = $reporter4Param9) or (@name = $reporter4Param10) or (@name = $reporter4Param11) or (@name = $reporter4Param12) or (@name = $reporter4Param13) or (@name = $reporter4Param14) or (@name = $reporter4Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter5) and (($reporter5allParam = 1) or (@name = $reporter5Param1) or (@name = $reporter5Param2) or (@name = $reporter5Param3) or (@name = $reporter5Param4) or (@name = $reporter5Param5) or (@name = $reporter5Param6) or (@name = $reporter5Param7) or (@name = $reporter5Param8) or (@name = $reporter5Param9) or (@name = $reporter5Param10) or (@name = $reporter5Param11) or (@name = $reporter5Param12) or (@name = $reporter5Param13) or (@name = $reporter5Param14) or (@name = $reporter5Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter6) and (($reporter6allParam = 1) or (@name = $reporter6Param1) or (@name = $reporter6Param2) or (@name = $reporter6Param3) or (@name = $reporter6Param4) or (@name = $reporter6Param5) or (@name = $reporter6Param6) or (@name = $reporter6Param7) or (@name = $reporter6Param8) or (@name = $reporter6Param9) or (@name = $reporter6Param10) or (@name = $reporter6Param11) or (@name = $reporter6Param12) or (@name = $reporter6Param13) or (@name = $reporter6Param14) or (@name = $reporter6Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter7) and (($reporter7allParam = 1) or (@name = $reporter7Param1) or (@name = $reporter7Param2) or (@name = $reporter7Param3) or (@name = $reporter7Param4) or (@name = $reporter7Param5) or (@name = $reporter7Param6) or (@name = $reporter7Param7) or (@name = $reporter7Param8) or (@name = $reporter7Param9) or (@name = $reporter7Param10) or (@name = $reporter7Param11) or (@name = $reporter7Param12) or (@name = $reporter7Param13) or (@name = $reporter7Param14) or (@name = $reporter7Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter8) and (($reporter8allParam = 1) or (@name = $reporter8Param1) or (@name = $reporter8Param2) or (@name = $reporter8Param3) or (@name = $reporter8Param4) or (@name = $reporter8Param5) or (@name = $reporter8Param6) or (@name = $reporter8Param7) or (@name = $reporter8Param8) or (@name = $reporter8Param9) or (@name = $reporter8Param10) or (@name = $reporter8Param11) or (@name = $reporter8Param12) or (@name = $reporter8Param13) or (@name = $reporter8Param14) or (@name = $reporter8Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter9) and (($reporter9allParam = 1) or (@name = $reporter9Param1) or (@name = $reporter9Param2) or (@name = $reporter9Param3) or (@name = $reporter9Param4) or (@name = $reporter9Param5) or (@name = $reporter9Param6) or (@name = $reporter9Param7) or (@name = $reporter9Param8) or (@name = $reporter9Param9) or (@name = $reporter9Param10) or (@name = $reporter9Param11) or (@name = $reporter9Param12) or (@name = $reporter9Param13) or (@name = $reporter9Param14) or (@name = $reporter9Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                            <xsl:when
                                    test="(../../@type = $reporter10) and (($reporter10allParam = 1) or (@name = $reporter10Param1) or (@name = $reporter10Param2) or (@name = $reporter10Param3) or (@name = $reporter10Param4) or (@name = $reporter10Param5) or (@name = $reporter10Param6) or (@name = $reporter10Param7) or (@name = $reporter10Param8) or (@name = $reporter10Param9) or (@name = $reporter10Param10) or (@name = $reporter10Param11) or (@name = $reporter10Param12) or (@name = $reporter10Param13) or (@name = $reporter10Param14) or (@name = $reporter10Param15))">
                                <xsl:call-template name="paramHeading"/>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:for-each>
                </tr>
                <xsl:apply-templates/>
            </table>
            <br/>
            <br/>
            <br/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="//item">
        <tr valign="top">
            <td>
                <xsl:value-of select="@name"/>
            </td>
            <xsl:apply-templates/>
        </tr>
    </xsl:template>
    <xsl:template match="//item/param">
        <xsl:choose>
            <xsl:when test="$allReporters = 1">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter1) and (($reporter1allParam = 1) or (@name = $reporter1Param1) or (@name = $reporter1Param2) or (@name = $reporter1Param3) or (@name = $reporter1Param4) or (@name = $reporter1Param5) or (@name = $reporter1Param6) or (@name = $reporter1Param7) or (@name = $reporter1Param8) or (@name = $reporter1Param9) or (@name = $reporter1Param10) or (@name = $reporter1Param11) or (@name = $reporter1Param12) or (@name = $reporter1Param13) or (@name = $reporter1Param14) or (@name = $reporter1Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter2) and (($reporter2allParam = 1) or (@name = $reporter2Param1) or (@name = $reporter2Param2) or (@name = $reporter2Param3) or (@name = $reporter2Param4) or (@name = $reporter2Param5) or (@name = $reporter2Param6) or (@name = $reporter2Param7) or (@name = $reporter2Param8) or (@name = $reporter2Param9) or (@name = $reporter2Param10) or (@name = $reporter2Param11) or (@name = $reporter2Param12) or (@name = $reporter2Param13) or (@name = $reporter2Param14) or (@name = $reporter2Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter3) and (($reporter3allParam = 1) or (@name = $reporter3Param1) or (@name = $reporter3Param2) or (@name = $reporter3Param3) or (@name = $reporter3Param4) or (@name = $reporter3Param5) or (@name = $reporter3Param6) or (@name = $reporter3Param7) or (@name = $reporter3Param8) or (@name = $reporter3Param9) or (@name = $reporter3Param10) or (@name = $reporter3Param11) or (@name = $reporter3Param12) or (@name = $reporter3Param13) or (@name = $reporter3Param14) or (@name = $reporter3Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter4) and (($reporter4allParam = 1) or (@name = $reporter4Param1) or (@name = $reporter4Param2) or (@name = $reporter4Param3) or (@name = $reporter4Param4) or (@name = $reporter4Param5) or (@name = $reporter4Param6) or (@name = $reporter4Param7) or (@name = $reporter4Param8) or (@name = $reporter4Param9) or (@name = $reporter4Param10) or (@name = $reporter4Param11) or (@name = $reporter4Param12) or (@name = $reporter4Param13) or (@name = $reporter4Param14) or (@name = $reporter4Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter5) and (($reporter5allParam = 1) or (@name = $reporter5Param1) or (@name = $reporter5Param2) or (@name = $reporter5Param3) or (@name = $reporter5Param4) or (@name = $reporter5Param5) or (@name = $reporter5Param6) or (@name = $reporter5Param7) or (@name = $reporter5Param8) or (@name = $reporter5Param9) or (@name = $reporter5Param10) or (@name = $reporter5Param11) or (@name = $reporter5Param12) or (@name = $reporter5Param13) or (@name = $reporter5Param14) or (@name = $reporter5Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter6) and (($reporter6allParam = 1) or (@name = $reporter6Param1) or (@name = $reporter6Param2) or (@name = $reporter6Param3) or (@name = $reporter6Param4) or (@name = $reporter6Param5) or (@name = $reporter6Param6) or (@name = $reporter6Param7) or (@name = $reporter6Param8) or (@name = $reporter6Param9) or (@name = $reporter6Param10) or (@name = $reporter6Param11) or (@name = $reporter6Param12) or (@name = $reporter6Param13) or (@name = $reporter6Param14) or (@name = $reporter6Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter7) and (($reporter7allParam = 1) or (@name = $reporter7Param1) or (@name = $reporter7Param2) or (@name = $reporter7Param3) or (@name = $reporter7Param4) or (@name = $reporter7Param5) or (@name = $reporter7Param6) or (@name = $reporter7Param7) or (@name = $reporter7Param8) or (@name = $reporter7Param9) or (@name = $reporter7Param10) or (@name = $reporter7Param11) or (@name = $reporter7Param12) or (@name = $reporter7Param13) or (@name = $reporter7Param14) or (@name = $reporter7Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter8) and (($reporter8allParam = 1) or (@name = $reporter8Param1) or (@name = $reporter8Param2) or (@name = $reporter8Param3) or (@name = $reporter8Param4) or (@name = $reporter8Param5) or (@name = $reporter8Param6) or (@name = $reporter8Param7) or (@name = $reporter8Param8) or (@name = $reporter8Param9) or (@name = $reporter8Param10) or (@name = $reporter8Param11) or (@name = $reporter8Param12) or (@name = $reporter8Param13) or (@name = $reporter8Param14) or (@name = $reporter8Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter9) and (($reporter9allParam = 1) or (@name = $reporter9Param1) or (@name = $reporter9Param2) or (@name = $reporter9Param3) or (@name = $reporter9Param4) or (@name = $reporter9Param5) or (@name = $reporter9Param6) or (@name = $reporter9Param7) or (@name = $reporter9Param8) or (@name = $reporter9Param9) or (@name = $reporter9Param10) or (@name = $reporter9Param11) or (@name = $reporter9Param12) or (@name = $reporter9Param13) or (@name = $reporter9Param14) or (@name = $reporter9Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when
                    test="(../../@type = $reporter10) and (($reporter10allParam = 1) or (@name = $reporter10Param1) or (@name = $reporter10Param2) or (@name = $reporter10Param3) or (@name = $reporter10Param4) or (@name = $reporter10Param5) or (@name = $reporter10Param6) or (@name = $reporter10Param7) or (@name = $reporter10Param8) or (@name = $reporter10Param9) or (@name = $reporter10Param10) or (@name = $reporter10Param11) or (@name = $reporter10Param12) or (@name = $reporter10Param13) or (@name = $reporter10Param14) or (@name = $reporter10Param15))">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
            <xsl:when test="../../../param/item">
                <xsl:call-template name="paramOutput"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="paramHeading">
        <th>
            <xsl:value-of select="@name"/>
        </th>
    </xsl:template>
    <xsl:template name="paramOutput">
        <td>
            <xsl:if test="not(item)">
                <xsl:value-of select="."/>
            </xsl:if>
            <xsl:if test="item">
                <table>
                    <tr>
                        <th>name</th>
                        <xsl:for-each select="item[1]/param">
                            <th>
                                <xsl:value-of select="@name"/>
                            </th>
                        </xsl:for-each>
                    </tr>
                    <xsl:apply-templates/>
                </table>
            </xsl:if>
        </td>
    </xsl:template>
</xsl:stylesheet>