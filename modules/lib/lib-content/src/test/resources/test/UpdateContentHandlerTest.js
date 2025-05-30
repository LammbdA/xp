var assert = require('/lib/xp/testing.js');
var content = require('/lib/xp/content.js');

var expectedJson = {
    '_id': '123456',
    '_name': 'mycontent',
    '_path': '/a/b/mycontent',
    'creator': 'user:system:admin',
    'modifier': 'user:system:admin',
    'createdTime': '1970-01-01T00:00:00Z',
    'modifiedTime': '1970-01-01T00:00:00Z',
    'type': 'test:myContentType',
    'displayName': 'Modified',
    'hasChildren': false,
    'language': 'es',
    'valid': false,
    'data': {
        'a': 2,
        'b': '2',
        'c': [
            {
                'd': true
            },
            {
                'd': false,
                'e': [
                    '3',
                    '42',
                    '5'
                ],
                'f': 2
            }
        ],
        'z': '99'
    },
    'x': {
        'com-enonic-myapplication': {
            'myschema': {
                'a': 1
            },
            'other': {
                'name': 'test'
            }
        }
    },
    'page': {
        'type': 'page',
        'path': '/',
        'descriptor': 'my-app-key:mycontroller',
        'config': {
            'a': '1'
        },
        'regions': {
            'top': {
                'components': [
                    {
                        'path': '/top/0',
                        'type': 'part',
                        'descriptor': 'app-descriptor-x:name-x',
                        'config': {
                            'a': '1'
                        }
                    },
                    {
                        'path': '/top/1',
                        'type': 'layout',
                        'descriptor': 'layoutDescriptor:name',
                        'config': {},
                        'regions': {
                            'left': {
                                'components': [
                                    {
                                        'path': '/top/1/left/0',
                                        'type': 'part',
                                        'config': {}
                                    },
                                    {
                                        'path': '/top/1/left/1',
                                        'type': 'text',
                                        'text': 'text text text'
                                    },
                                    {
                                        'path': '/top/1/left/2',
                                        'type': 'text',
                                        'text': ''
                                    }
                                ],
                                name: 'left'
                            },
                            'right': {
                                'components': [
                                    {
                                        'path': '/top/1/right/0',
                                        'type': 'image',
                                        'image': 'image-id'
                                    },
                                    {
                                        'path': '/top/1/right/1',
                                        'type': 'fragment',
                                        'fragment': '213sda-ss222'
                                    }
                                ],
                                name: 'right'
                            }
                        }
                    },
                    {
                        'path': '/top/2',
                        'type': 'layout',
                        'config': {},
                        'regions': {}
                    }
                ],
                name: 'top'
            },
            'bottom': {
                'components': [
                    {
                        'path': '/bottom/0',
                        'type': 'part',
                        'descriptor': 'app-descriptor-y:name-y',
                        'config': {
                            'a': '1'
                        }
                    },
                    {
                        'path': '/bottom/1',
                        'type': 'image',
                        'image': 'img-id-x'
                    },
                    {
                        'path': '/bottom/2',
                        'type': 'image'
                    }
                ],
                name: 'bottom'
            }
        }
    },
    'attachments': {},
    'publish': {
        'from': '2018-11-03T10:00:01Z',
        'to': '2018-11-03T10:00:01Z',
    },
    'workflow': {
        'state': 'READY',
        'checks': {
            'Review by marketing': 'APPROVED'
        }
    }
};

function globalEditor(c) {
    c.displayName = 'Modified';
    c.data.a++;
    c.data.z = '99';
    c.data.c[1].d = false;
    c.data.c[1].e[1] = 42;

    if (!c.x['com-enonic-myapplication']) {
        c.x['com-enonic-myapplication'] = {};
    }

    c.x['com-enonic-myapplication'].other = {
        name: 'test'
    };

    c.language = 'es';

    c.publish.from = '2018-11-03T10:00:01Z';
    c.publish.to = '2018-11-03T10:00:01Z';
    c.workflow.state = 'READY';
    c.workflow.checks = {
        'Review by marketing': 'APPROVED'
    };

    return c;
}

exports.update_notFound = function () {
    var result = content.update({
        key: '123456',
        editor: globalEditor
    });

    assert.assertNull(result);
};

exports.updateById = function () {
    var result = content.update({
        key: '123456',
        editor: globalEditor
    });

    assert.assertJsonEquals(expectedJson, result);
};

exports.updateByPath = function () {
    var result = content.update({
        key: '/a/b/mycontent',
        editor: globalEditor
    });

    assert.assertJsonEquals(expectedJson, result);
};

exports.updateSiteConfig = function () {

    var result = content.update({
        key: '/a/b/mycontent',
        editor(c) {
            c.data.siteConfig = [];
            c.data.siteConfig[0] = {};
            c.data.siteConfig[0].applicationKey = 'appKey1';
            c.data.siteConfig[0].config = {};
            c.data.siteConfig[0].config.a = 'a';
            c.data.siteConfig[0].config.b = true;
            c.data.siteConfig[1] = {};
            c.data.siteConfig[1].applicationKey = 'appKey2';
            c.data.siteConfig[1].config = {};
            c.data.siteConfig[1].config.c = '4';

            return c;
        }
    });

    var expect = [
        {
            'applicationKey': 'appKey1',
            'config': {
                'a': 'a',
                'b': true
            }
        },
        {
            'applicationKey': 'appKey2',
            'config': {
                'c': 4
            }
        }
    ];

    assert.assertJsonEquals(expect, result.data.siteConfig);
};

exports.updateSiteSingleDescriptor = function () {

    var result = content.update({
        key: '/a/b/mycontent',
        editor(c) {
            c.data.siteConfig = {};
            c.data.siteConfig.applicationKey = 'appKey1';
            c.data.siteConfig.config = {};
            c.data.siteConfig.config.a = 'a';
            c.data.siteConfig.config.b = true;

            return c;
        }
    });

    var expect =
        {
            'applicationKey': 'appKey1',
            'config': {
                'a': 'a',
                'b': true
            }
        }
    ;

    assert.assertJsonEquals(expect, result.data.siteConfig);
};

exports.updateSiteConfig_strict = function () {

    try {
        content.update({
            key: '/a/b/mycontent',
            editor(c) {
                c.data.siteConfig = [];
                c.data.siteConfig[0] = {};
                c.data.siteConfig[0].applicationKey = 'appKey1';
                c.data.siteConfig[0].config = {};
                c.data.siteConfig[0].config.invalidField = 'a';

                return c;
            }
        });
        throw new Error();
    } catch (e) {
        assert.assertEquals('No mapping defined for property invalidField with value a', e.getMessage());
    }
};

exports.updateNotMappedXDataFieldName_stricted = function () {

    try {
        var result = content.update({
            key: '/a/b/mycontent',
            editor(c) {
                globalEditor(c);
                c.x['com-enonic-myapplication'].other = {
                    name: 'test',
                    notMappedField: 'value'
                };

                return c;
            }
        });

        throw new Error();
    } catch (e) {
        assert.assertEquals('No mapping defined for property notMappedField with value value', e.getMessage());
    }


};

exports.updateNotMappedXDataFieldName_notStricted = function () {

    var result = content.update({
        key: '/a/b/mycontent',
        requireValid: false,
        editor(c) {
            globalEditor(c);
            c.x['com-enonic-myapplication'].other = {
                name: 'test',
                notMappedField: 'value'
            };

            return c;
        }
    });

    assert.assertNotNull(result);
};
