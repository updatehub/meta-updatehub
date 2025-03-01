SUMMARY = "updatehub - Active/Inactive U-Boot backend"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://COPYING;md5=838c366f69b72c5df05c96dff79b35f2"

SRC_URI = "git://github.com/updatehub/active-inactive-backend-u-boot.git;protocol=https;branch=master"
SRCREV = "e64ce459d879c2da7224c1eb8d3ea88a1192ea3b"

S = "${WORKDIR}/git"

PV = "0.0+${SRCPV}"

inherit allarch

do_configure[noexec] = "1"

do_install () {
    install -Dm 0755 updatehub-active-get ${D}${bindir}/updatehub-active-get
    install -Dm 0755 updatehub-active-set ${D}${bindir}/updatehub-active-set
    install -Dm 0755 updatehub-active-validated ${D}${bindir}/updatehub-active-validated
}

RDEPENDS:${PN} += "u-boot-fw-utils u-boot-default-env"
INSANE_SKIP:${PN} += "build-deps"
