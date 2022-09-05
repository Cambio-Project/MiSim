package desmoj.extensions.visualization2d.engine.command;

/**
 * This interface describe the command syntax.
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public interface CommandSyntax {

    // ---to fit for each command, fit also Command ---------------------

    // this syntax-table must be completed
    String[][] CMD_SYNTAX = {
        {"createModelBasisData", "?", "ProjectName", "?", "ProjectURL", "?", "ProjectIconId", "1", "ModelName", "1",
            "ModelAuthor", "?", "ModelDate", "?", "ModelDescription", "*", "ModelRemark", "?", "ModelLicense", "?",
            "DesmojVersion", "?", "DesmojLicense", "?", "DesmojLicenseURL", ""},
        {"createSimTimeBounds", "1", "Begin", "?", "End", "?", "TimeZone", "1", "Speed", "speed ist nur Vorschlag"},
        {"createImage", "1", "ImageId", "1", "File", "relativ zum IconPath, set in Model"},
        {"createEntityTyp", "1", "EntityTypId", "1", "IconWidth", "1", "IconHeigth", "+", "PossibleState", "*",
            "PossibleAttribute", "1", "Show", ""},
        {"createEntity", "1", "EntityId", "?", "Name", "1", "EntityTypeId", "?", "Velocity", "1", "State", "*",
            "Attribute", "?", "Position", "value-structure Position:x|y|angle|direction"},
        {"setEntity", "1", "EntityId", "?", "Velocity", "?", "State", "*", "Attribute", ""},
        {"disposeEntity", "1", "EntityId", ""},
        {"createList", "1", "ListId", "?", "Name", "1", "DefaultEntityType", "1", "NumberOfVisible", "1", "Form", "1",
            "Point", "?", "DeltaSize", "?", "Comment",
            "value-structure Point:x|y and DeltaSize:dx|dy	Comment:textstring|fontSize|fontStyle|elementSizeExt|red|green|blue"},
        {"setList", "1", "ListId", "*", "AddEntity", "*", "AddEntityAfter", "*", "AddEntityBefore", "*", "RemoveEntity",
            "?", "RemoveAll",
            "value-structure AddEntity:EntityId|priority|priorityRule	AddEntityAfter:EntityId|priority|afterId	AddEntityBefore:EntityId|priority|bforeId  "},
        {"createStation", "1", "StationId", "?", "Name", "1", "Point", "value-structure Point:x|y"},
        {"createRoute", "1", "RouteId", "?", "Name", "1", "Length", "1", "SourceStationId", "1", "SinkStationId", "*",
            "Point", "1", "Show", "?", "Color", "?", "LineSize", "value-structure Point:x|y"},
        {"setRoute", "1", "RouteId", "*", "AddEntity", "*", "RemoveEntity", "?", "RemoveAll", "?", "Show", "?", "Color",
            "?", "LineSize", ""},
        {"createProcess", "1", "ProcessId", "?", "Name", "1", "NumberOfResEntity", "1", "NumberOfProcEntity", "1",
            "DefaultResEntityType", "1", "DefaultProcEntityType", "?", "ListId", "1", "Form", "1", "Point", "?",
            "DeltaSize", "remark Point:x|y"},
        {"setProcess", "1", "ProcessId", "*", "AddResEntity", "*", "RemoveResEntity", "*", "AddProcEntity", "*",
            "RemoveProcEntity", ""},
        {"createProcessNew", "1", "ProcessId", "?", "Name", "?", "Abstract", "?", "ResourceType", "?", "ResourceTotal",
            "?", "ListId", "1", "Point", "1", "DefaultEntityType", "1", "AnzVisible", "1", "Form", "?", "ShowResources",
            "?", "DeltaSize", "?", "Comment",
            "remark Point:x|y and DeltaSize:dx|dy  Comment:textstring|fontSize|fontStyle|elementSizeExt|red|green|blue"},
        {"setProcessNew", "1", "ProcessId", "*", "AddProcEntity", "*", "AddResEntity", "*", "AddResAnz", "*",
            "RemoveEntity", "remark "},
        {"createResource", "1", "ResourceId", "?", "Name", "?", "ResourceType", "1", "ResourceTotal", "1", "Point", "1",
            "DefaultEntityType", "1", "AnzVisible", "1", "Form", "?", "DeltaSize",
            "remark Point:x|y and deltaSize:dx|dy"},
        {"setResource", "1", "ResourceId", "?", "Provide", "?", "TakeProcess", "?", "TakeBackProcess",
            "value-structure Provide:EntityId|priority|AnzResources|sortorder and TakeBackProcess:EntityId|AnzResources"},
        {"createStock", "1", "StockId", "?", "Name", "1", "Capacity", "1", "InitialUnits", "1", "Point", "1",
            "DefaultEntityType", "1", "AnzVisible", "1", "Form", "?", "DeltaSize",
            "remark Point:x|y and deltaSize:dx|dy"},
        {"setStock", "1", "StockId", "?", "RetrieveBegin", "?", "RetrieveEnd", "?", "StoreBegin", "?", "StoreEnd",
            "value-structure RetrieveBegin:EntityId|priority|AnzResources|sortorder and StoreBegin:EntityId|priority|AnzResources|sortorder"},
        {"createBin", "1", "BinId", "?", "Name", "1", "InitialUnits", "1", "Point", "1", "DefaultEntityType", "1",
            "AnzVisible", "1", "Form", "?", "DeltaSize", "remark Point:x|y and deltaSize:dx|dy"},
        {"setBin", "1", "BinId", "?", "RetrieveBegin", "?", "RetrieveEnd", "?", "Store",
            "value-structure RetrieveBegin:EntityId|priority|AnzResources|sortorder"},
        {"createWaitQueue", "1", "WaitQueueId", "?", "Name", "1", "Point", "1", "DefaultEntityType", "1", "AnzVisible",
            "1", "Form", "?", "DeltaSize", "remark Point:x|y and deltaSize:dx|dy"},
        {"setWaitQueue", "1", "WaitQueueId", "?", "InsertMaster", "?", "InsertSlave", "?", "CooperationBegin", "?",
            "CooperationEnd",
            "value-structure CooperationBegin:MasterId|SlaveId  und CooperationEnd analog  and InsertMaster:EntityId|priority|sortorder and InsertSlave analog"},
        {"createStatistic", "1", "StatisticId", "?", "Name", "1", "TypeData", "1", "TypeIndex", "?", "Aggregate", "1",
            "TimeBounds", "1", "ValueBounds", "1", "HistogramCells", "1", "Point", "1", "TypeAnimation", "?",
            "IsIntValue", "?", "DeltaSize",
            "remark Point:x|y and DeltaSize:dx|dy and TimeBounds:low|high and ValueBounds:low|high"},
        {"setStatistic", "1", "StatisticId", "1", "Value", ""},
        {"resetStatistic", "1", "StatisticId", ""},
        {"createBackgroundElement", "1", "BgElemId", "?", "Name", "1", "Text", "?", "TopLeft", "?", "BottomRight", "?",
            "Middle", "?", "Size", "?", "Foreground", "?", "Background", "1", "Level", "?", "ImageId",
            "remark Text:textstring|position|fontSize|fontStyle, TopLeft:x|y , BottomRight:x|y , Middle:x|y , Size:width|heigth, Foreground:red|green|blue and Background:red|green|blue"},
        {"setBackgroundElement", "1", "BgElemId", "?", "Name", "1", "Text", "?", "Foreground", "?", "Background", "?",
            "ImageId", "remark Text:textstring|position|fontSize|fontStyle"},
        {"createBackgroundLine", "1", "BgLineId", "?", "LineSize", "?", "Color", "1", "StartPoint", "*", "AddPoint",
            "1", "Level", "remark Color:red|green|blue  StartPoint:layer|x|y and AddPoint:x|y "},
        {"end", "artifical included command for end of simulation"}
    };
    // ---to fit for each command, fit also Command ---------------------


}
